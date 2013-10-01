package com.skvortsov.mtproto.interfaces;
import com.skvortsov.mtproto.types.FriendInfo;
import com.skvortsov.mtproto.types.MessageInfo;


public interface IUpdateData {
	public void updateData(MessageInfo[] messages, FriendInfo[] friends, FriendInfo[] unApprovedFriends, String userKey);

}
