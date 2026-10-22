package com.school.dto;

/**
 * 管理员重置密码结果：返回被重置的用户与生成的一次性密码
 */
public class ResetPasswordResult {

    private String username;
    private String loginNo;
    private String realName;
    /** 一次性随机密码：只在本次响应中返回，服务端不保存明文 */
    private String oneTimePassword;
    /** 该密码的有效期（小时），便于前端提示 */
    private long validHours;

    public ResetPasswordResult() {
    }

    public ResetPasswordResult(String username, String loginNo, String realName,
                               String oneTimePassword, long validHours) {
        this.username = username;
        this.loginNo = loginNo;
        this.realName = realName;
        this.oneTimePassword = oneTimePassword;
        this.validHours = validHours;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLoginNo() {
        return loginNo;
    }

    public void setLoginNo(String loginNo) {
        this.loginNo = loginNo;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getOneTimePassword() {
        return oneTimePassword;
    }

    public void setOneTimePassword(String oneTimePassword) {
        this.oneTimePassword = oneTimePassword;
    }

    public long getValidHours() {
        return validHours;
    }

    public void setValidHours(long validHours) {
        this.validHours = validHours;
    }
}
