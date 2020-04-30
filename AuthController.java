package jp.co.internous.hope.controller;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import jp.co.internous.hope.model.domain.MstUser;
import jp.co.internous.hope.model.form.UserForm;
import jp.co.internous.hope.model.mapper.MstUserMapper;
import jp.co.internous.hope.model.mapper.TblCartMapper;
import jp.co.internous.hope.model.session.LoginSession;

@RestController
@RequestMapping("/hope")
public class AuthController {

	@Autowired
	private MstUserMapper mstUserMapper;

	@Autowired
	private LoginSession loginSession;

	@Autowired
	private TblCartMapper tblCartMapper;

	private Gson gson = new Gson();

	@RequestMapping("/auth/login")
	public String login(@RequestBody UserForm f) {
		MstUser user = mstUserMapper.findByUserNameAndPassword(f.getUserName(), f.getPassword());

		int tmpUserId = loginSession.getTmpUserId();

		if (user != null && tmpUserId != 0) {
			int count = tblCartMapper.findCountByUserId(tmpUserId);

			if (count > 0) {
				tblCartMapper.updateUserId(user.getId(), tmpUserId);
			}
		}
		// MstUserMapperにより検索した結果がある場合＝ユーザーが存在する場合

		if (user != null) {
			loginSession.setTmpUserId(0);
			loginSession.setLogined(true);
			loginSession.setUserId(user.getId());
			loginSession.setUserName(user.getUserName());
			loginSession.setPassword(user.getPassword());

		} else {
			// ユーザーが存在しない場合
			loginSession.setLogined(false);
			loginSession.setUserId(0);
			loginSession.setUserName(null);
			loginSession.setPassword(null);

		}
		return gson.toJson(user);
	}

	// ログアウト機能
	@RequestMapping("/auth/logout")
	// LoginSessionの内容をログアウト状態に変更
	public String logout() {
		loginSession.setTmpUserId(0);
		loginSession.setLogined(false);
		loginSession.setUserId(0);
		loginSession.setUserName(null);
		loginSession.setPassword(null);

		return "";
	}

	// パスワード再設定機能
	@RequestMapping("/auth/resetPassword")
	public String resetPassword(@RequestBody UserForm f) {
		String message = "パスワードが再設定されました。";
		String newPassword = f.getNewPassword();
		String newPasswordConfirm = f.getNewPasswordConfirm();

		MstUser user = mstUserMapper.findByUserNameAndPassword(f.getUserName(), f.getPassword());
		if (user == null) {
			return "現在のパスワードが正しくありません。";
		}

		if (user.getPassword().equals(newPassword)) {
			return "現在のパスワードと同一文字列が入力されました。";
		}

		if (!newPassword.equals(newPasswordConfirm)) {
			return "新パスワードと確認用パスワードが一致しません。";
		}
		mstUserMapper.updatePassword(user.getUserName(), f.getNewPassword());
		loginSession.setPassword(f.getNewPassword());
		return message;
	}
}