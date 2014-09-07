/*
 * The MIT License (MIT)
 * Copyright (c) 2011-2014 Turenai Project
 *
 * Permission is hereby granted, free of charge,
 *  to any person obtaining a copy of this software and associated documentation files (the "Software"),
 *  to deal in the Software without restriction, including without limitation the rights to
 *  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 *  and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 *  in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 *  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 *  DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package jp.mydns.turenar.twclient.impl;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

import twitter4j.AccountSettings;
import twitter4j.Category;
import twitter4j.DirectMessage;
import twitter4j.Friendship;
import twitter4j.GeoLocation;
import twitter4j.GeoQuery;
import twitter4j.IDs;
import twitter4j.Location;
import twitter4j.OEmbed;
import twitter4j.OEmbedRequest;
import twitter4j.PagableResponseList;
import twitter4j.Paging;
import twitter4j.Place;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.RateLimitStatus;
import twitter4j.RateLimitStatusListener;
import twitter4j.Relationship;
import twitter4j.ResponseList;
import twitter4j.SavedSearch;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Trends;
import twitter4j.Twitter;
import twitter4j.TwitterAPIConfiguration;
import twitter4j.TwitterException;
import twitter4j.UploadedMedia;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.api.DirectMessagesResources;
import twitter4j.api.FavoritesResources;
import twitter4j.api.FriendsFollowersResources;
import twitter4j.api.HelpResources;
import twitter4j.api.ListsResources;
import twitter4j.api.PlacesGeoResources;
import twitter4j.api.SavedSearchesResources;
import twitter4j.api.SearchResource;
import twitter4j.api.SpamReportingResource;
import twitter4j.api.SuggestedUsersResources;
import twitter4j.api.TimelinesResources;
import twitter4j.api.TrendsResources;
import twitter4j.api.TweetsResources;
import twitter4j.api.UsersResources;
import twitter4j.auth.AccessToken;
import twitter4j.auth.Authorization;
import twitter4j.auth.OAuth2Token;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;

/**
 * Twitter Implementation for BlockingUsersChannelTest
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public abstract class AbstractTwitter implements Twitter {
	@Override
	public void addRateLimitStatusListener(RateLimitStatusListener listener) {

	}

	@Override
	public User createBlock(long userId) throws TwitterException {
		return null;
	}

	@Override
	public User createBlock(String screenName) throws TwitterException {
		return null;
	}

	@Override
	public Status createFavorite(long id) throws TwitterException {
		return null;
	}

	@Override
	public User createFriendship(long userId) throws TwitterException {
		return null;
	}

	@Override
	public User createFriendship(String screenName) throws TwitterException {
		return null;
	}

	@Override
	public User createFriendship(long userId, boolean follow) throws TwitterException {
		return null;
	}

	@Override
	public User createFriendship(String screenName, boolean follow) throws TwitterException {
		return null;
	}

	@Override
	public User createMute(long userId) throws TwitterException {
		return null;
	}

	@Override
	public User createMute(String screenName) throws TwitterException {
		return null;
	}

	@Override
	public SavedSearch createSavedSearch(String query) throws TwitterException {
		return null;
	}

	@Override
	public UserList createUserList(String listName, boolean isPublicList,
			String description) throws TwitterException {
		return null;
	}

	@Override
	public UserList createUserListMember(long listId, long userId) throws TwitterException {
		return null;
	}

	@Override
	public UserList createUserListMember(long ownerId, String slug, long userId) throws TwitterException {
		return null;
	}

	@Override
	public UserList createUserListMember(String ownerScreenName, String slug,
			long userId) throws TwitterException {
		return null;
	}

	@Override
	public UserList createUserListMembers(long listId, long[] userIds) throws TwitterException {
		return null;
	}

	@Override
	public UserList createUserListMembers(long ownerId, String slug,
			long[] userIds) throws TwitterException {
		return null;
	}

	@Override
	public UserList createUserListMembers(String ownerScreenName, String slug,
			long[] userIds) throws TwitterException {
		return null;
	}

	@Override
	public UserList createUserListMembers(long listId, String[] screenNames) throws TwitterException {
		return null;
	}

	@Override
	public UserList createUserListMembers(long ownerId, String slug,
			String[] screenNames) throws TwitterException {
		return null;
	}

	@Override
	public UserList createUserListMembers(String ownerScreenName, String slug,
			String[] screenNames) throws TwitterException {
		return null;
	}

	@Override
	public UserList createUserListSubscription(long listId) throws TwitterException {
		return null;
	}

	@Override
	public UserList createUserListSubscription(long ownerId, String slug) throws TwitterException {
		return null;
	}

	@Override
	public UserList createUserListSubscription(String ownerScreenName,
			String slug) throws TwitterException {
		return null;
	}

	@Override
	public User destroyBlock(long userId) throws TwitterException {
		return null;
	}

	@Override
	public User destroyBlock(String screen_name) throws TwitterException {
		return null;
	}

	@Override
	public DirectMessage destroyDirectMessage(long id) throws TwitterException {
		return null;
	}

	@Override
	public Status destroyFavorite(long id) throws TwitterException {
		return null;
	}

	@Override
	public User destroyFriendship(long userId) throws TwitterException {
		return null;
	}

	@Override
	public User destroyFriendship(String screenName) throws TwitterException {
		return null;
	}

	@Override
	public User destroyMute(long userId) throws TwitterException {
		return null;
	}

	@Override
	public User destroyMute(String screenName) throws TwitterException {
		return null;
	}

	@Override
	public SavedSearch destroySavedSearch(int id) throws TwitterException {
		return null;
	}

	@Override
	public Status destroyStatus(long statusId) throws TwitterException {
		return null;
	}

	@Override
	public UserList destroyUserList(long listId) throws TwitterException {
		return null;
	}

	@Override
	public UserList destroyUserList(long ownerId, String slug) throws TwitterException {
		return null;
	}

	@Override
	public UserList destroyUserList(String ownerScreenName, String slug) throws TwitterException {
		return null;
	}

	@Override
	public UserList destroyUserListMember(long listId, long userId) throws TwitterException {
		return null;
	}

	@Override
	public UserList destroyUserListMember(long listId, String screenName) throws TwitterException {
		return null;
	}

	@Override
	public UserList destroyUserListMember(long ownerId, String slug, long userId) throws TwitterException {
		return null;
	}

	@Override
	public UserList destroyUserListMember(String ownerScreenName, String slug,
			long userId) throws TwitterException {
		return null;
	}

	@Override
	public UserList destroyUserListMembers(long listId, String[] screenNames) throws TwitterException {
		return null;
	}

	@Override
	public UserList destroyUserListMembers(long listId, long[] userIds) throws TwitterException {
		return null;
	}

	@Override
	public UserList destroyUserListMembers(String ownerScreenName, String slug,
			String[] screenNames) throws TwitterException {
		return null;
	}

	@Override
	public UserList destroyUserListSubscription(long listId) throws TwitterException {
		return null;
	}

	@Override
	public UserList destroyUserListSubscription(long ownerId, String slug) throws TwitterException {
		return null;
	}

	@Override
	public UserList destroyUserListSubscription(String ownerScreenName,
			String slug) throws TwitterException {
		return null;
	}

	@Override
	public DirectMessagesResources directMessages() {
		return null;
	}

	@Override
	public FavoritesResources favorites() {
		return null;
	}

	@Override
	public FriendsFollowersResources friendsFollowers() {
		return null;
	}

	@Override
	public TwitterAPIConfiguration getAPIConfiguration() throws TwitterException {
		return null;
	}

	@Override
	public AccountSettings getAccountSettings() throws TwitterException {
		return null;
	}

	@Override
	public Authorization getAuthorization() {
		return null;
	}

	@Override
	public ResponseList<Location> getAvailableTrends() throws TwitterException {
		return null;
	}

	@Override
	public IDs getBlocksIDs() throws TwitterException {
		return null;
	}

	@Override
	public IDs getBlocksIDs(long cursor) throws TwitterException {
		return null;
	}

	@Override
	public PagableResponseList<User> getBlocksList() throws TwitterException {
		return null;
	}

	@Override
	public PagableResponseList<User> getBlocksList(long cursor) throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<Location> getClosestTrends(GeoLocation location) throws TwitterException {
		return null;
	}

	@Override
	public Configuration getConfiguration() {
		return null;
	}

	@Override
	public ResponseList<User> getContributees(long userId) throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<User> getContributees(String screenName) throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<User> getContributors(long userId) throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<User> getContributors(String screenName) throws TwitterException {
		return null;
	}

	@Override
	public InputStream getDMImageAsStream(String url) throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<DirectMessage> getDirectMessages() throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<DirectMessage> getDirectMessages(Paging paging) throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<Status> getFavorites() throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<Status> getFavorites(long userId) throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<Status> getFavorites(String screenName) throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<Status> getFavorites(Paging paging) throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<Status> getFavorites(long userId, Paging paging) throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<Status> getFavorites(String screenName, Paging paging) throws TwitterException {
		return null;
	}

	@Override
	public IDs getFollowersIDs(long userId, long cursor, int count) throws TwitterException {
		return null;
	}

	@Override
	public IDs getFollowersIDs(String screenName, long cursor, int count) throws TwitterException {
		return null;
	}

	@Override
	public IDs getFollowersIDs(long cursor) throws TwitterException {
		return null;
	}

	@Override
	public IDs getFollowersIDs(long userId, long cursor) throws TwitterException {
		return null;
	}

	@Override
	public IDs getFollowersIDs(String screenName, long cursor) throws TwitterException {
		return null;
	}

	@Override
	public PagableResponseList<User> getFollowersList(long userId, long cursor, int count, boolean skipStatus,
			boolean includeUserEntities) throws TwitterException {
		return null;
	}

	@Override
	public PagableResponseList<User> getFollowersList(String screenName, long cursor, int count, boolean skipStatus,
			boolean includeUserEntities) throws TwitterException {
		return null;
	}

	@Override
	public PagableResponseList<User> getFollowersList(long userId, long cursor) throws TwitterException {
		return null;
	}

	@Override
	public PagableResponseList<User> getFollowersList(String screenName,
			long cursor) throws TwitterException {
		return null;
	}

	@Override
	public PagableResponseList<User> getFollowersList(long userId, long cursor,
			int count) throws TwitterException {
		return null;
	}

	@Override
	public PagableResponseList<User> getFollowersList(String screenName, long cursor,
			int count) throws TwitterException {
		return null;
	}

	@Override
	public IDs getFriendsIDs(long userId, long cursor, int count) throws TwitterException {
		return null;
	}

	@Override
	public IDs getFriendsIDs(String screenName, long cursor, int count) throws TwitterException {
		return null;
	}

	@Override
	public IDs getFriendsIDs(long cursor) throws TwitterException {
		return null;
	}

	@Override
	public IDs getFriendsIDs(long userId, long cursor) throws TwitterException {
		return null;
	}

	@Override
	public IDs getFriendsIDs(String screenName, long cursor) throws TwitterException {
		return null;
	}

	@Override
	public PagableResponseList<User> getFriendsList(long userId, long cursor, int count) throws TwitterException {
		return null;
	}

	@Override
	public PagableResponseList<User> getFriendsList(String screenName, long cursor, int count) throws TwitterException {
		return null;
	}

	@Override
	public PagableResponseList<User> getFriendsList(long userId, long cursor, int count, boolean skipStatus,
			boolean includeUserEntities) throws TwitterException {
		return null;
	}

	@Override
	public PagableResponseList<User> getFriendsList(String screenName, long cursor, int count, boolean skipStatus,
			boolean includeUserEntities) throws TwitterException {
		return null;
	}

	@Override
	public PagableResponseList<User> getFriendsList(long userId, long cursor) throws TwitterException {
		return null;
	}

	@Override
	public PagableResponseList<User> getFriendsList(String screenName,
			long cursor) throws TwitterException {
		return null;
	}

	@Override
	public Place getGeoDetails(String placeId) throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<Status> getHomeTimeline() throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<Status> getHomeTimeline(Paging paging) throws TwitterException {
		return null;
	}

	@Override
	public long getId() throws TwitterException, IllegalStateException {
		return 0;
	}

	@Override
	public IDs getIncomingFriendships(long cursor) throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<Language> getLanguages() throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<User> getMemberSuggestions(String categorySlug) throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<Status> getMentionsTimeline() throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<Status> getMentionsTimeline(Paging paging) throws TwitterException {
		return null;
	}

	@Override
	public IDs getMutesIDs(long cursor) throws TwitterException {
		return null;
	}

	@Override
	public PagableResponseList<User> getMutesList(long cursor) throws TwitterException {
		return null;
	}

	@Override
	public IDs getNoRetweetsFriendships() throws TwitterException {
		return null;
	}

	@Override
	public OAuth2Token getOAuth2Token() throws TwitterException {
		return null;
	}

	@Override
	public AccessToken getOAuthAccessToken() throws TwitterException {
		return null;
	}

	@Override
	public AccessToken getOAuthAccessToken(String oauthVerifier) throws TwitterException {
		return null;
	}

	@Override
	public AccessToken getOAuthAccessToken(RequestToken requestToken) throws TwitterException {
		return null;
	}

	@Override
	public AccessToken getOAuthAccessToken(RequestToken requestToken,
			String oauthVerifier) throws TwitterException {
		return null;
	}

	@Override
	public AccessToken getOAuthAccessToken(String screenName, String password) throws TwitterException {
		return null;
	}

	@Override
	public RequestToken getOAuthRequestToken() throws TwitterException {
		return null;
	}

	@Override
	public RequestToken getOAuthRequestToken(String callbackURL) throws TwitterException {
		return null;
	}

	@Override
	public RequestToken getOAuthRequestToken(String callbackURL,
			String xAuthAccessType) throws TwitterException {
		return null;
	}

	@Override
	public OEmbed getOEmbed(OEmbedRequest req) throws TwitterException {
		return null;
	}

	@Override
	public IDs getOutgoingFriendships(long cursor) throws TwitterException {
		return null;
	}

	@Override
	public Trends getPlaceTrends(int woeid) throws TwitterException {
		return null;
	}

	@Override
	public String getPrivacyPolicy() throws TwitterException {
		return null;
	}

	@Override
	public Map<String, RateLimitStatus> getRateLimitStatus() throws TwitterException {
		return null;
	}

	@Override
	public Map<String, RateLimitStatus> getRateLimitStatus(String... resources) throws TwitterException {
		return null;
	}

	@Override
	public IDs getRetweeterIds(long statusId, long cursor) throws TwitterException {
		return null;
	}

	@Override
	public IDs getRetweeterIds(long statusId, int count, long cursor) throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<Status> getRetweets(long statusId) throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<Status> getRetweetsOfMe() throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<Status> getRetweetsOfMe(Paging paging) throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<SavedSearch> getSavedSearches() throws TwitterException {
		return null;
	}

	@Override
	public String getScreenName() throws TwitterException, IllegalStateException {
		return null;
	}

	@Override
	public ResponseList<DirectMessage> getSentDirectMessages() throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<DirectMessage> getSentDirectMessages(Paging paging) throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<Place> getSimilarPlaces(GeoLocation location, String name, String containedWithin,
			String streetAddress) throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<Category> getSuggestedUserCategories() throws TwitterException {
		return null;
	}

	@Override
	public String getTermsOfService() throws TwitterException {
		return null;
	}

	@Override
	public PagableResponseList<User> getUserListMembers(long listId, long cursor) throws TwitterException {
		return null;
	}

	@Override
	public PagableResponseList<User> getUserListMembers(long ownerId, String slug,
			long cursor) throws TwitterException {
		return null;
	}

	@Override
	public PagableResponseList<User> getUserListMembers(String ownerScreenName, String slug,
			long cursor) throws TwitterException {
		return null;
	}

	@Override
	public PagableResponseList<UserList> getUserListMemberships(long cursor) throws TwitterException {
		return null;
	}

	@Override
	public PagableResponseList<UserList> getUserListMemberships(long listMemberId,
			long cursor) throws TwitterException {
		return null;
	}

	@Override
	public PagableResponseList<UserList> getUserListMemberships(String listMemberScreenName,
			long cursor) throws TwitterException {
		return null;
	}

	@Override
	public PagableResponseList<UserList> getUserListMemberships(String listMemberScreenName, long cursor,
			boolean filterToOwnedLists) throws TwitterException {
		return null;
	}

	@Override
	public PagableResponseList<UserList> getUserListMemberships(long listMemberId, long cursor,
			boolean filterToOwnedLists) throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<Status> getUserListStatuses(long listId, Paging paging) throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<Status> getUserListStatuses(long ownerId, String slug,
			Paging paging) throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<Status> getUserListStatuses(String ownerScreenName, String slug,
			Paging paging) throws TwitterException {
		return null;
	}

	@Override
	public PagableResponseList<User> getUserListSubscribers(long listId,
			long cursor) throws TwitterException {
		return null;
	}

	@Override
	public PagableResponseList<User> getUserListSubscribers(long ownerId, String slug,
			long cursor) throws TwitterException {
		return null;
	}

	@Override
	public PagableResponseList<User> getUserListSubscribers(String ownerScreenName, String slug,
			long cursor) throws TwitterException {
		return null;
	}

	@Override
	public PagableResponseList<UserList> getUserListSubscriptions(String listOwnerScreenName,
			long cursor) throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<UserList> getUserLists(String listOwnerScreenName) throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<UserList> getUserLists(long listOwnerUserId) throws TwitterException {
		return null;
	}

	@Override
	public PagableResponseList<UserList> getUserListsOwnerships(String listOwnerScreenName, int count,
			long cursor) throws TwitterException {
		return null;
	}

	@Override
	public PagableResponseList<UserList> getUserListsOwnerships(long listOwnerId, int count,
			long cursor) throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<User> getUserSuggestions(String categorySlug) throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<Status> getUserTimeline(String screenName, Paging paging) throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<Status> getUserTimeline(long userId, Paging paging) throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<Status> getUserTimeline(String screenName) throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<Status> getUserTimeline(long userId) throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<Status> getUserTimeline() throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<Status> getUserTimeline(Paging paging) throws TwitterException {
		return null;
	}

	@Override
	public HelpResources help() {
		return null;
	}

	@Override
	public void invalidateOAuth2Token() throws TwitterException {

	}

	@Override
	public ListsResources list() {
		return null;
	}

	@Override
	public ResponseList<Status> lookup(long[] ids) throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<Friendship> lookupFriendships(long[] ids) throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<Friendship> lookupFriendships(String[] screenNames) throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<User> lookupUsers(long[] ids) throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<User> lookupUsers(String[] screenNames) throws TwitterException {
		return null;
	}

	@Override
	public PlacesGeoResources placesGeo() {
		return null;
	}

	@Override
	public void removeProfileBanner() throws TwitterException {

	}

	@Override
	public User reportSpam(long userId) throws TwitterException {
		return null;
	}

	@Override
	public User reportSpam(String screenName) throws TwitterException {
		return null;
	}

	@Override
	public Status retweetStatus(long statusId) throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<Place> reverseGeoCode(GeoQuery query) throws TwitterException {
		return null;
	}

	@Override
	public SavedSearchesResources savedSearches() {
		return null;
	}

	@Override
	public SearchResource search() {
		return null;
	}

	@Override
	public QueryResult search(Query query) throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<Place> searchPlaces(GeoQuery query) throws TwitterException {
		return null;
	}

	@Override
	public ResponseList<User> searchUsers(String query, int page) throws TwitterException {
		return null;
	}

	@Override
	public DirectMessage sendDirectMessage(long userId, String text) throws TwitterException {
		return null;
	}

	@Override
	public DirectMessage sendDirectMessage(String screenName, String text) throws TwitterException {
		return null;
	}

	@Override
	public void setOAuth2Token(OAuth2Token oauth2Token) {

	}

	@Override
	public void setOAuthAccessToken(AccessToken accessToken) {

	}

	@Override
	public void setOAuthConsumer(String consumerKey, String consumerSecret) {

	}

	@Override
	public DirectMessage showDirectMessage(long id) throws TwitterException {
		return null;
	}

	@Override
	public Relationship showFriendship(long sourceId, long targetId) throws TwitterException {
		return null;
	}

	@Override
	public Relationship showFriendship(String sourceScreenName,
			String targetScreenName) throws TwitterException {
		return null;
	}

	@Override
	public SavedSearch showSavedSearch(int id) throws TwitterException {
		return null;
	}

	@Override
	public Status showStatus(long id) throws TwitterException {
		return null;
	}

	@Override
	public User showUser(long userId) throws TwitterException {
		return null;
	}

	@Override
	public User showUser(String screenName) throws TwitterException {
		return null;
	}

	@Override
	public UserList showUserList(long listId) throws TwitterException {
		return null;
	}

	@Override
	public UserList showUserList(long ownerId, String slug) throws TwitterException {
		return null;
	}

	@Override
	public UserList showUserList(String ownerScreenName, String slug) throws TwitterException {
		return null;
	}

	@Override
	public User showUserListMembership(long listId, long userId) throws TwitterException {
		return null;
	}

	@Override
	public User showUserListMembership(long ownerId, String slug, long userId) throws TwitterException {
		return null;
	}

	@Override
	public User showUserListMembership(String ownerScreenName, String slug,
			long userId) throws TwitterException {
		return null;
	}

	@Override
	public User showUserListSubscription(long listId, long userId) throws TwitterException {
		return null;
	}

	@Override
	public User showUserListSubscription(long ownerId, String slug, long userId) throws TwitterException {
		return null;
	}

	@Override
	public User showUserListSubscription(String ownerScreenName, String slug,
			long userId) throws TwitterException {
		return null;
	}

	@Override
	public SpamReportingResource spamReporting() {
		return null;
	}

	@Override
	public SuggestedUsersResources suggestedUsers() {
		return null;
	}

	@Override
	public TimelinesResources timelines() {
		return null;
	}

	@Override
	public TrendsResources trends() {
		return null;
	}

	@Override
	public TweetsResources tweets() {
		return null;
	}

	@Override
	public AccountSettings updateAccountSettings(Integer trendLocationWoeid, Boolean sleepTimeEnabled,
			String startSleepTime, String endSleepTime, String timeZone,
			String lang) throws TwitterException {
		return null;
	}

	@Override
	public Relationship updateFriendship(long userId, boolean enableDeviceNotification,
			boolean retweets) throws TwitterException {
		return null;
	}

	@Override
	public Relationship updateFriendship(String screenName, boolean enableDeviceNotification,
			boolean retweets) throws TwitterException {
		return null;
	}

	@Override
	public User updateProfile(String name, String url, String location,
			String description) throws TwitterException {
		return null;
	}

	@Override
	public User updateProfileBackgroundImage(File image, boolean tile) throws TwitterException {
		return null;
	}

	@Override
	public User updateProfileBackgroundImage(InputStream image, boolean tile) throws TwitterException {
		return null;
	}

	@Override
	public void updateProfileBanner(File image) throws TwitterException {

	}

	@Override
	public void updateProfileBanner(InputStream image) throws TwitterException {

	}

	@Override
	public User updateProfileColors(String profileBackgroundColor, String profileTextColor,
			String profileLinkColor,
			String profileSidebarFillColor, String profileSidebarBorderColor) throws TwitterException {
		return null;
	}

	@Override
	public User updateProfileImage(File image) throws TwitterException {
		return null;
	}

	@Override
	public User updateProfileImage(InputStream image) throws TwitterException {
		return null;
	}

	@Override
	public Status updateStatus(String status) throws TwitterException {
		return null;
	}

	@Override
	public Status updateStatus(StatusUpdate latestStatus) throws TwitterException {
		return null;
	}

	@Override
	public UserList updateUserList(long listId, String newListName, boolean isPublicList,
			String newDescription) throws TwitterException {
		return null;
	}

	@Override
	public UserList updateUserList(long ownerId, String slug, String newListName, boolean isPublicList,
			String newDescription) throws TwitterException {
		return null;
	}

	@Override
	public UserList updateUserList(String ownerScreenName, String slug, String newListName,
			boolean isPublicList,
			String newDescription) throws TwitterException {
		return null;
	}

	@Override
	public UploadedMedia uploadMedia(File mediaFile) throws TwitterException {
		return null;
	}

	@Override
	public UsersResources users() {
		return null;
	}

	@Override
	public User verifyCredentials() throws TwitterException {
		return null;
	}
}
