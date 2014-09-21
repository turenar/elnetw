#!/bin/bash
# vim: ts=4:sw=4

PROJECT_DIR=$(pwd)
PROJECT_NAME=elnetw

function main() {
	parse_args $@

	check_dirty_state
	test_bool $_mode_release && prepare_release
	test_bool $_mode_release && do_release
	test_bool $_mode_package && do_package
	test_bool $_mode_release && post_release

	echo "done."
}

function prepare_release() {
	local __ignore __version __date
	echo "preparing for release ${_release_ver}..."
	_debug "> checking changelog..."
	# check changelog
	# Version <version> (<date>)
	IFS=' ()' read __ignore __version __ignore __date < ChangeLog.txt
	if [ "$__version" != "$_release_ver" ]; then
		echo "ChangeLog.txt has ${__version}. please edit!"
		die "Error: illegal changelog version: $__version"
	elif [ -n "$__date" ]; then
		echo "Changelog.txt has already released date"
		die "Error: illegal changelog released date: $__date"
	fi

	_debug "> checking existing tag..."
	if git show-ref --tags --quiet -- "v${_release_ver}"; then
		echo "v${_release_ver} has already released. Check tags!"
		die "Error: illegal release version"
	fi

	_debug "> updating changelog..."
	sed -i -e '1s/$/ ('"$(LC_ALL=C LANG=C date -R)"')/' ChangeLog.txt
	rewrite_package_version ${_release_ver}

	_mvn clean
}

function do_package() {
	echo "packaging..."

	test_bool $_opt_sandbox && enter_sandbox

	_debug "> removing findbugs-annotations dependency..."
	find -maxdepth 2 -name pom.xml | xargs sed -i -e 's@flag: ignore-packaging.*@@' -e 's@.*flag: /ignore-packaging@@'

	_debug "> removing findbugs annotations..."
	find */src/*/java -type f | xargs sed -i -e '/edu.umd.cs.findbugs.annotations\|javax.annotation/ d' -e 's/@Nonnull\|@Nullable\|@SuppressFBWarnings\((.*)\)\?//g'

	_mvn clean
	
	_debug "> getting most closed tag name..."
	local __tag_name __described
	__tag_name="$(git describe --abbrev=0)"
	if [ "${__tag_name}" == "$(git describe)" ]; then
		__described="${__tag_name#v}"
	else
		local __commit_count __rev
		__commit_count="$(git log --oneline ${__tag_name}..HEAD | wc -l)"
		__rev="$(git rev-parse --short HEAD)"
		__described="${__tag_name#v}+dev-${__commit_count}-g${__rev}"
	fi

	_mvn package $(if_bool $_mode_sign -Psign) -DdescribedVersion=${__described}
	
	_debug "> re-package tar.gz as zip"
	cd elnetw-launcher/target
	mkdir package.sh.tmp
	cd package.sh.tmp
	__filename=../elnetw-*-bin.tar.gz
	tar -zxf ../elnetw-*-bin.tar.gz
	zip -q -r ${__filename%.tar.gz}.zip *
	cd ../../..
	_debug "> saving binary package..."
	mv elnetw-launcher/target/elnetw-*-bin.tar.gz ${PROJECT_DIR}/elnetw-bin-${__described}.tar.gz
	mv elnetw-launcher/target/elnetw-*-bin.zip ${PROJECT_DIR}/elnetw-bin-${__described}.zip
	
	test_bool $_opt_sandbox && leave_sandbox
}

function do_release() {
	local __git_opt
	echo "releasing..."

	_mvn test

	_debug "> committing release..."
	git ci -a -m "[release.sh] release version ${_release_ver}" || exit 1

	_debug "> tagging..."
	__git_opt=( -a -s -m"[release.sh] release version ${_release_ver}" )
	test "${_tag_key}" = "" || __git_opt+=(-u ${_tag_key})
	git tag "${__git_opt[@]}" v${_release_ver} || exit 1
}

function post_release() {
	echo "cleaning up..."
	rewrite_package_version ${_release_newver}
	_debug "> committing for ${_release_newver}"
	git ci -a -m "[release.sh] prepare for ${_release_newver}" || exit 1
}

function _mvn() {
	_debug "> exec mvn $@ ${_mvn_opts}"
	mvn $@ ${_mvn_opts} >${PROJECT_DIR}/tmp.maven.log 2>&1
	if [ $? -ne 0 ]; then
		cat ${PROJECT_DIR}/tmp.maven.log
		exit 1
	fi
	rm ${PROJECT_DIR}/tmp.maven.log
}

function rewrite_package_version() {
	_debug "> rewriting package version..."
	find -maxdepth 2 -name pom.xml | xargs sed -e '1,20 s@\(<version>\).\+\(</version>\)@\1'"$1"'\2@' -i
}

function check_dirty_state() {
	local __uncommitted_files_count
	_debug "> checking if cwd is project dir..."
	test -d .git || die "current working directory is not in project dir"
	
	_debug "> checking whether project is clean state..."
	# check dirty state
	__uncommitted_files_count=$(git status --porcelain 2>/dev/null | egrep "^(M| M)" | wc -l)
	if [ ${__uncommitted_files_count} -ne 0 ]; then
		echo "project is dirty!" >&2
		test_bool $_opt_force || die "remains ${__uncommitted_files_count} uncommitted file(s)"
	fi
}

function enter_sandbox() {
	check_dirty_state

	_debug "> entering sandbox..."
	# prepare clean git directory
	test -d target || mkdir target
	test -d target/release_tmp && rm -rf target/release_tmp
	git clone .git target/release_tmp --reference .git || exit 1
	cd target/release_tmp
	_sandbox_in=true
}

function leave_sandbox() {
	test $_sandbox_in || return 0

	_debug "> leaving sandbox..."
	cd ${PROJECT_DIR}
	rm -rf target/release_tmp
	_sandbox_in=false
}

function die() {
	echo $1 >&2
	exit 1
}

function test_bool() {
	test "$1" = "true"
	return $?
}

function if_bool() {
	test_bool $1 && echo $2
}

function _debug() {
	test_bool $_opt_verbose && echo $@
}


function __default_args() {
	_opt_sandbox=true
}

function ensure_arg() {
	test -n "$2" || die "argument required for '$1'"
}

function parse_args() {
	local __mode_selected __opt __arg __saved_arg __arg_consumed __should_shift_arg
	__default_args

	while [ -n "$1" ]; do
		__arg_consumed=''
		__should_shift_arg='' # if true and __arg_consumed==true, shift argument
		if [ -n "${__saved_arg}" ]; then # -abcdef...
			__opt="-${__saved_arg:0:1}"
			__arg="${__saved_arg:1}"
			__saved_arg="$__arg"
		elif [[ "$1" == --*=* ]]; then # --hoge=fuga
			__opt=${1%%=*}
			__arg="${1#*=}"
		elif [[ "$1" == --* ]]; then # --hoge [fuga]
			__opt="$1"
			__arg="$2"
			__should_shift_arg=true
		elif [[ "$1" == -??* ]]; then # -abcdef...: first pass
			__saved_arg="${1:1}"
			continue
		elif [[ "$1" == -? ]]; then # -a
			__opt="$1"
			__arg="$2"
			__should_shift_arg=true
		else
			die "Unknown argument: $1"
		fi
		# echo -- ${__opt} ${__arg} >&2
		case "${__opt}" in
			-p|--package)
				__mode_selected=true
				_mode_package=true;;
			-r|--release)
				__mode_selected=true
				_mode_release=true;;
			-t|--release-version)
				ensure_arg ${__opt} ${__arg}
				__arg_consumed=true
				_release_ver=${__arg};;
			-N|--new-version)
				ensure_arg ${__opt} ${__arg}
				__arg_consumed=true
				_release_newver=${__arg};;
			-S|--no-sandbox)
				_opt_sandbox=false;;
			--sandbox)
				_opt_sandbox=true;;
			-s|--sign|--signature)
				_mode_sign=true;;
			-k|--tag-key)
				ensure_arg ${__opt} ${__arg}
				__arg_consumed=true
				_tag_key=${__arg};;
			-v|--verbose)
				_opt_verbose=true;;
			-f|--force)
				_opt_force=true;;
			-M|--mvn-opts)
				ensure_arg ${__opt} ${__arg}
				__arg_consumed=true
				_mvn_opts="${_mvn_opts} ${__arg}";;
			-*)
				die "Unknown option: ${__opt}";;
			*)
				die # dead code
		esac

		if [ -n "${__arg_consumed}" ]; then
			# if arg is consumed and saved_arg is not null, saved_arg is arg
			__saved_arg=''
			if [ -n "$__should_shift_arg" ]; then
				shift
			fi
		fi

		if [ -z "$__saved_arg" ]; then
			shift
		fi
	done

	test_bool $__mode_selected || die "--package or --release is required"
	if test_bool $_mode_release; then
		if test -z "$_release_ver"; then
			local __ignore __version __date
			# check changelog
			# Version <version> (<date>)
			IFS=' ()' read __ignore __version __ignore __date < ChangeLog.txt
			if test -n "${__date}"; then
				die "automatic release version detection failed: seems to be already released"
			fi
			echo "Use ${__version} as releasing version"
			_release_ver="${__version}"
		fi
		if test -z "$_release_newver"; then
			local __pom_version __new_version
			__pom_version=$(grep '<version>' pom.xml | head -n1 | sed -e '1s@.\+<version>\(.\+\)</version>@\1@')
			echo -n "Input new snapshot version [$__pom_version]> "
			read __new_version
			if test -z ${__new_version}; then
				_release_newver=${__pom_version}
			else
				_release_newver=${__new_version}
			fi
		fi
	fi
}


main "$@"
