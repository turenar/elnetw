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
}

function prepare_release() {
	echo "preparing for release ${_release_ver}..."
	rewrite_package_version ${_release_ver}
	_mvn clean
}

function do_package() {
	echo "packaging..."

	test_bool $_opt_sandbox && enter_sandbox

	_debug "> removing findbugs-annotations dependency..."
	find -maxdepth 2 -name pom.xml | xargs sed -i -e 's@flag: ignore-packaging.*@@' -e 's@.*flag: /ignore-packaging@@'

	_debug "> removing findbugs annotations..."
	find */src/*/java -type f | xargs sed -i -e '/@edu.umd.cs.findbugs.annotations\|javax.annotation/ d' -e 's/@Nonnull\|@Nullable//g'

	_mvn clean
	_mvn package $(if_bool $_mode_sign -Psign)
	
	_debug "> save binary package"
	test_bool $_sandbox_in && mv elnetw-launcher/target/elnetw-*-bin.tar.gz ${PROJECT_DIR}
	
	test_bool $_opt_sandbox && leave_sandbox
}

function do_release() {
	local __git_opt
	echo "releasing..."
	_mvn test

	_debug "> commit release..."
	git ci -a -m "[release.sh] release version ${_release_ver}" || exit 1

	_debug "> tagging..."
	__git_opt=( -a -s -m"[release.sh] release version ${_release_ver}" )
	test "${_tag_key}" = "" || __git_opt+=(-u ${_tag_key})
	git tag "${__git_opt[@]}" v${_release_ver} || exit 1
}

function post_release() {
	echo "cleaning up..."
	rewrite_package_version ${_release_newver}
	git ci -a -m "[release.sh] prepare for ${_release_newver}" || exit 1
}

function _mvn() {
	_debug "> exec mvn $@"
	mvn $@ >${PROJECT_DIR}/tmp.maven.log 2>&1
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

function parse_args() {
	local __mode_selected
	__default_args
	while test -n "$1"; do
		case "$1" in
			--package)
				__mode_selected=true
				_mode_package=true;;
			--release)
				__mode_selected=true
				_mode_release=true;;
			--release-version)
				shift
				_release_ver=$1;;
			--new-version)
				shift
				_release_newver=$1;;
			--no-sandbox)
				_opt_sandbox=false;;
			--sandbox)
				_opt_sandbox=true;;
			--signature)
				_mode_sign=true;;
			--tag-key)
				shift
				_tag_key=$1;;
			--verbose)
				_opt_verbose=true;;
			--force)
				_opt_force=true;;
			-*)
				die "Unknown option: $1";;
			*)
				die "Unknown argument: $1";;
		esac

		shift
	done

	test_bool $__mode_selected || die "--package or --release is required"
	if test_bool $_mode_release; then
		if test -z "$_release_ver" -o -z "$_release_newver"; then
			die "'--release' option requires '--release-version' and '--new-version'"
		fi
	fi
}


main $@
