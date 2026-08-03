package com.school.common;

/**
 * 用户状态常量（对应 sys_user.status）
 */
public final class UserStatus {

    /** 初始：账号已创建但未设置初始密码（首次登录需设置密码） */
    public static final int INITIAL = 0;

    /** 正常 */
    public static final int NORMAL = 1;

    /** 请假 */
    public static final int LEAVE = 2;

    /** 休学 */
    public static final int SUSPEND = 3;

    /** 转出 */
    public static final int TRANSFERRED = 4;

    /** 离职 */
    public static final int RESIGNED = 5;

    /** 病假 */
    public static final int SICK_LEAVE = 6;

    /** 退休 */
    public static final int RETIRED = 7;

    /** 毕业 */
    public static final int GRADUATED = 8;

    /** 锁定：密码连续错误被锁定，需管理员重置 */
    public static final int LOCKED = 9;

    /** 密码过期：当前时间晚于 pwd_update_time（修改密码时间+90天），需强制修改密码 */
    public static final int PASSWORD_EXPIRED = 10;

    private UserStatus() {
    }
}
