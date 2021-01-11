package controller;

import java.util.Random;
import bean.UserInfo;
import bean.UserPet;
import game.DB;
import game.Player;
import pers.jc.network.SocketComponent;
import pers.jc.network.SocketMethod;
import pers.jc.sql.SQL;
import pers.jc.sql.Transaction;
import result.GashaponResult;
import result.RequestResult;

@SocketComponent("GashaponController")
public class GashaponController {
	static int[] R = new int[] {6004};
	static int[] SR = new int[] {6005};
	static int[] SSR = new int[] {6006, 6017, 6104, 6131, 6133};
	static int[] SP = new int[] {6095, 6113, 6115, 6140};
	
	public Random random = new Random();
	
	@SocketMethod
	public RequestResult excuteGashapon(Player player, int type) {
		RequestResult requestResult = new RequestResult();
		UserInfo userInfo = (UserInfo) player.userInfo.clone();
		if (type == 1) {
			if (userInfo.getCoin() < 8000) {
				requestResult.setMsg("��Ҳ���");
				return requestResult;
			} else {
				userInfo.setCoin(userInfo.getCoin() - 8000);
			}
		}
		if (type == 2 || type == 3) {
			if (userInfo.getDiamond() < 128) {
				requestResult.setMsg("��ʯ����");
				return requestResult;
			} else {
				userInfo.setDiamond(userInfo.getDiamond() - 128);
			}
		}
		int pet_id = 0;
		int randomValue = random.nextInt(100);
		if (type == 1){
			if (randomValue < 90) {
				pet_id = R[random.nextInt(R.length)];
			} else if (randomValue < 100) {
				pet_id = SR[random.nextInt(SR.length)];
			}
		}
		if (type == 2){
//			if (randomValue < 75) {
//				pet_id = R[random.nextInt(R.length)];
//			} else if (randomValue < 95) {
//				pet_id = SR[random.nextInt(SR.length)];
//			} else if (randomValue < 99) {
//				pet_id = SSR[random.nextInt(SSR.length)];
//			} else if (randomValue < 100) {
//				pet_id = SP[random.nextInt(SP.length)];
//			}
			if (randomValue < 25) {
				pet_id = R[random.nextInt(R.length)];
			} else if (randomValue < 50) {
				pet_id = SR[random.nextInt(SR.length)];
			} else if (randomValue < 75) {
				pet_id = SSR[random.nextInt(SSR.length)];
			} else if (randomValue < 100) {
				pet_id = SP[random.nextInt(SP.length)];
			}
		}
		if (type == 3){
			if (randomValue < 25) {
				pet_id = R[random.nextInt(R.length)];
			} else if (randomValue < 50) {
				pet_id = SR[random.nextInt(SR.length)];
			} else if (randomValue < 75) {
				pet_id = SSR[random.nextInt(SSR.length)];
			} else if (randomValue < 100) {
				pet_id = 6095;
			}
		}
		int pet_id_copy = pet_id;
		UserPet userPet = DB.curd.selectOne(UserPet.class, new SQL(){{
			WHERE("user_id=" + PARAM(userInfo.getId()));
			WHERE("pet_id=" + PARAM(pet_id_copy));
		}});
		if (userPet == null) {
			userPet = new UserPet();
			userPet.setUser_id(userInfo.getId());
			userPet.setPet_id(pet_id_copy);
		} else {
			userPet.setFragment(userPet.getFragment() + 1);
		}
		UserPet userPet_copy = userPet;
		new Transaction(DB.curd.getAccess()) {
			@Override
			public void run() throws Exception {
				update(userInfo);
				if (userPet_copy.getId() == 0) {
					insertAndGenerateKeys(userPet_copy);
				} else {
					update(userPet_copy);
				}
				commit();
			}
			@Override
			public void success() {
				player.userInfo = userInfo;
				GashaponResult gashaponResult = new GashaponResult(userInfo, userPet_copy);
				requestResult.setCode(200);
				requestResult.setData(gashaponResult);
				requestResult.setMsg("Ť���ɹ�");
			}
			@Override
			public void fail() {
				requestResult.setMsg("Ť��ʧ��");
			}
		};
		return requestResult;
	}
}
