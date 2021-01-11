package controller;

import java.util.ArrayList;

import bean.Friend;
import bean.FriendChat;
import bean.FriendChatPublic;
import bean.FriendMapper;
import game.DB;
import game.Player;
import game.PlayerMgr;
import pers.jc.engine.JCEntity;
import pers.jc.network.SocketComponent;
import pers.jc.network.SocketMethod;
import pers.jc.sql.SQL;
import pers.jc.util.JCUtil;

@SocketComponent("FriendController")
public class FriendController {
	
	@SocketMethod
	public static String addFriend(Player player, int invitee_id) {
		ArrayList<FriendMapper> list = DB.curd.select(FriendMapper.class, new SQL() {{
			WHERE("inviter_id=" + player.userInfo.getId() + " and " + "invitee_id=" + invitee_id);
			OR();
			WHERE("invitee_id=" + player.userInfo.getId() + " and " + "inviter_id=" + invitee_id);
			System.out.println(this);
		}});
		if (list.size() > 0) {
			return "�����Ѿ��Ǻ�����";
		} else {
			FriendMapper friendMapper = new FriendMapper();
			friendMapper.setInviter_id(player.userInfo.getId());
			friendMapper.setInvitee_id(invitee_id);
			DB.curd.insert(friendMapper);
			return "������ӳɹ�";
		}
	}
	
	@SocketMethod
	public static ArrayList<Friend> loadFriends(Player player) {
		ArrayList<Friend> list = new ArrayList<>();
		ArrayList<Friend> list1 = DB.curd.select(Friend.class, new SQL(){{
			WHERE("friend_mapper.inviter_id = " + player.userInfo.getId());
			WHERE("friend_mapper.invitee_id = user_info.id");
		}});
		ArrayList<Friend> list2 = DB.curd.select(Friend.class, new SQL(){{
			WHERE("friend_mapper.invitee_id = " + player.userInfo.getId());
			WHERE("friend_mapper.inviter_id = user_info.id");
		}});
		list.addAll(list1);
		list.addAll(list2);
		ArrayList<FriendChat> friendChats = DB.curd.select(FriendChat.class, new SQL() {{
			WHERE("receiver_id=" + player.userInfo.getId());
		}});
		for (Friend friend : list) {
			if (friend.getInviter_id() == player.userInfo.getId()) {
				friend.setFriend_id(friend.getInvitee_id());
			} else if (friend.getInvitee_id() == player.userInfo.getId()) {
				friend.setFriend_id(friend.getInviter_id());
			}
			for (FriendChat friendChat : friendChats) {
				if (friend.getFriend_id() == friendChat.getSender_id()) {
					friend.setNewMsgCount(friend.getNewMsgCount() + 1);
				}
			}
		}
		return list; 
	}
	
	@SocketMethod
	public static boolean deleteFriend(int friend_mapper_id) {
		int updateCount = DB.curd.delete(new SQL() {{
			DELETE_FROM("friend_mapper");
			WHERE("id = " + friend_mapper_id);
		}});
		if (updateCount == 1) {
			return true;
		} else {
			return false;
		}
	}
	
	@SocketMethod
	public static ArrayList<FriendChat> loadFriendMsg(Player player, int friend_id) {
		ArrayList<FriendChat> friendChats = DB.curd.select(FriendChat.class, new SQL() {{
			WHERE("sender_id=" + friend_id);
			WHERE("receiver_id=" + player.userInfo.getId());
		}});
		return friendChats;
	}

	@SocketMethod
	public static void sendPrivateMsg(Player player, int receiver_id, String msg) {
		FriendChat friendChat = new FriendChat();
		friendChat.setId(JCUtil.uuid());
		friendChat.setSender_id(player.userInfo.getId());
		friendChat.setReceiver_id(receiver_id);
		friendChat.setMessage(msg);
		player.call("receivePrivateMsg", friendChat);
		JCEntity other = PlayerMgr.get(receiver_id);
		if (other != null) {
			other.call("receivePrivateMsg", friendChat);
		}
		DB.curd.insert(friendChat);
	}
	
	@SocketMethod
	public static int readPrivateMsg(String[] msg_ids) {
		for (int i= 0; i < msg_ids.length; i++) {
			msg_ids[i] = "'" + msg_ids[i] + "'";
		}
		return DB.curd.delete(new SQL() {{
			DELETE_FROM("friend_chat");
			WHERE("id in (" + String.join(",", msg_ids) + ")");
		}});
	}	
	
	@SocketMethod
	public static void sendPublicMsg(Player player, String msg) {
		FriendChatPublic friendChatPublic = new FriendChatPublic(
			player.userInfo.getId(), 
			player.userInfo.getNickname(), 
			player.userInfo.getAvatarUrl(), 
			player.userInfo.getIntegral(), 
			msg
		);
		PlayerMgr.callAll("receivePublicMsg", friendChatPublic);
	}
}
