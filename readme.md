# 验证单点登录后台

## 文件夹介绍
security 文件夹中存放验证登录的信息

APIUserDetailsService 是jsp直接登录的时候，使用的。

PreUserDetailsService 是直接调用接口使用的

SpringSecurityConfig 的configure是配置拦截规则的

TokenPreAuthenticationFilter 中的SSO_TOKEN是调用接口时，使用的headers的key。

## 登录、登出
登录使用用户名，密码登录。

登出直接使用token进行登出。

## 拦截

默认除了登录接口，其他接口都进行拦截

## 测试工具

postman工具

