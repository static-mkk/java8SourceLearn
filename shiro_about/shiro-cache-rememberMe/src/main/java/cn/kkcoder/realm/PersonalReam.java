package cn.kkcoder.realm;

import cn.kkcoder.dao.UserDao;
import cn.kkcoder.domain.User;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * 定制的realm
 */
public class PersonalReam  extends AuthorizingRealm {

    static String KEY = "mkk";
   //这里注入userDao
    @Autowired
    private UserDao userDao;

    /**
     * 授权方法
     * @param principalCollection
     * @return
     */
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {

        String username = (String)principalCollection.getPrimaryPrincipal();

        // 模拟 通过用户名从数据库或者缓存中获取角色数据 和 权限数据
        Set<String> roles = getRolesByUsername(username);
        Set<String> permissions = getPermissionByUsername(username);

        //把用户数据和权限数据放到SimpleAuthorizationInfo中，用于授权验证的工作
        SimpleAuthorizationInfo simpleAuthorizationInfo  = new SimpleAuthorizationInfo();
        simpleAuthorizationInfo.setRoles(roles);
        simpleAuthorizationInfo.setStringPermissions(permissions);
        return simpleAuthorizationInfo;
    }

    /**
     * 模拟从数据库中获取权限数据
     * @return 获取到的权限数据的set集合
     */
    private Set<String> getPermissionByUsername(String username) {
        List<String> permissions = userDao.getPerssionsByUsername(username);
        Set<String> permissionsSet = new HashSet<String>(permissions);
        return permissionsSet;
    }

    /**
     * 从数据库中获取角色数据
     * @return 获取到的角色数据的set集合
     */
    private Set<String> getRolesByUsername(String username) {
        System.out.println("从数据库中获取信息");
        List<String> roles = userDao.getRoleByUsername(username);
        Set<String> rolesSet = new HashSet<String>(roles);
        return rolesSet;
    }

    /**
     * 认证方法
     * @param authenticationToken
     * @return
     * @throws AuthenticationException
     */
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        // authenticationToken 是主体传过来的用户信息 getPrincipal() 获取用户名信息
        String username = (String)authenticationToken.getPrincipal();

        // 根据用户名，从数据库或者缓存中获取密码
        String password = getPasswordByUserName(username);
        if( password == null ){
            return null;
        }

        /*

        //如果spring中没有注入凭证匹配器，这里可以手动设置一个
        CredentialsMatcher credentialsMatcher = new HashedCredentialsMatcher();
        //设置加密算法
        ((HashedCredentialsMatcher) credentialsMatcher).setHashAlgorithmName("md5");
        //设置加密次数
        ((HashedCredentialsMatcher) credentialsMatcher).setHashIterations(1);
        super.setCredentialsMatcher(credentialsMatcher);

        */

        // 这里说明用户名和密码都存在，所以创建一个AuthenticationInfo对象来用于返回，并设置好用户名和密码
        SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(username,password,"PersonRealm");
        //设置 salt
        authenticationInfo.setCredentialsSalt(ByteSource.Util.bytes(KEY.getBytes()));
        return authenticationInfo;
    }

    /**
     * 模拟数据库获取密码
     * @param username
     * @return
     */
    private String getPasswordByUserName(String username) {
        User user = userDao.getPasswordByUsername(username);
        if(user != null){
            return user.getPassword();
        }
        return null;
    }
}
