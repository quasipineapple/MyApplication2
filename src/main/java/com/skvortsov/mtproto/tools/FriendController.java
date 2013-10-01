package com.skvortsov.mtproto.tools;

import com.skvortsov.mtproto.types.FriendInfo;

/*
 * This class can store friendInfo and check userkey and username combination 
 * according to its stored data
 */
public class FriendController 
{
	
	private static FriendInfo[] friendsInfo = null;
	private static FriendInfo[] unapprovedFriendsInfo = null;
	private static String activeFriend;
	
	public static void setFriendsInfo(FriendInfo[] friendInfo)
	{
		FriendController.friendsInfo = friendInfo;
	}
	
	
	
	public static FriendInfo checkFriend(String username, String userKey)
	{
		FriendInfo result = null;
		if (friendsInfo != null) 
		{
            for (FriendInfo aFriendsInfo : friendsInfo) {
                if (aFriendsInfo.userName.equals(username) &&
                        aFriendsInfo.userKey.equals(userKey)
                        ) {
                    result = aFriendsInfo;
                    break;
                }
            }
		}		
		return result;
	}
	
	public static void setActiveFriend(String friendName){
		activeFriend = friendName;
	}
	
	public static String getActiveFriend()
	{
		return activeFriend;
	}



	public static FriendInfo getFriendInfo(String username) 
	{
		FriendInfo result = null;
		if (friendsInfo != null) 
		{
            for (FriendInfo aFriendsInfo : friendsInfo) {
                if (aFriendsInfo.userName.equals(username)) {
                    result = aFriendsInfo;
                    break;
                }
            }
		}		
		return result;
	}



	public static void setUnapprovedFriendsInfo(FriendInfo[] unapprovedFriends) {
		unapprovedFriendsInfo = unapprovedFriends;		
	}



	public static FriendInfo[] getFriendsInfo() {
		return friendsInfo;
	}



	public static FriendInfo[] getUnapprovedFriendsInfo() {
		return unapprovedFriendsInfo;
	}
	
	
	

}
