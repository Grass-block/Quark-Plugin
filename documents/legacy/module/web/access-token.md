# AccessToken | HTTP令牌

文：GrassBlock2022

----

作为大部分TCraftAPI要求的AccessToken(访问令牌)的生成模块，我们建议保持其开启。<br/>
当然不开启也是可以的，不过你也就只能用用查询API和开放了权限组的API了。

模块id:

```
quark_web:access_token
```

### 登录

利用Quark内建凭据系统关联浏览器用户和游戏玩家的身份，生成鉴权令牌以供后续API验证。

路径:

```
/auth/login
```

参数:

| 名称       | 值                 | 样例              |
|----------|-------------------|-----------------|
| name     | 玩家名               | GrassBlock2022  | 
| password | 凭据(第一次进游戏给你看的那玩意) | password@114514 |

结果:

```json
{
  "token": "(base 64 token)",
  //生成的Token值,留好它
  "lifetime": "1800"
  //剩余token存活时间
}
```

可能的错误:

| 状态码 | 错误ID              | 原因         |
|-----|-------------------|------------|
| 400 | PARAMETER_MISSING | 缺少玩家名或密码   |
| 400 | AUTHORIZE_FAILED  | 玩家不存在或密码错误 |

### 登出

销毁一个令牌,这个令牌将不再可以使用。

---

路径:

```
/auth/logout
```

参数:

| 名称    | 值       | 样例  |
|-------|---------|-----|
| token | 生成的访问令牌 | (略) |

结果:

```json
{
  "token": "(base 64 token)"
  //销毁的token，虽然这个值没啥用
}
```

可能的错误:

| 状态码 | 错误ID              | 原因                         |
|-----|-------------------|----------------------------|
| 400 | PARAMETER_MISSING | 缺少AccessToken              |
| 400 | INVALID_TOKEN     | 不是正确的AccessToken, 可能已被自动销毁 |

### 续期

可以手动延长一个token的期限。

---

路径:

```
/auth/extand
```

参数:

| 名称    | 值        | 样例   |
|-------|----------|------|
| token | 生成的访问令牌  | (略)  |
| time  | 延长的时间(秒) | 8000 |

结果:

```json
{
  "token": "(base 64 token)",
  //token值
  "remain": "2000"
  //剩余时间
}
```

可能的错误:

| 状态码 | 错误ID              | 原因                         |
|-----|-------------------|----------------------------|
| 400 | PARAMETER_MISSING | 缺少AccessToken              |
| 400 | INVALID_TOKEN     | 不是正确的AccessToken, 可能已被自动销毁 |