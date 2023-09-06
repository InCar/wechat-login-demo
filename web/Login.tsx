import React, { useState, useEffect } from 'react';
import { Button } from '@mui/material';
import axios from 'axios';

declare class WxLogin {
    constructor(params: any);
}

export const Login = () => {
    const [isLogin, setIsLogin] = useState(false);
    const [userInfo, setUserInfo] = useState<any>({});

    useEffect(() => {
        // 插入微信脚本
        const script = document.createElement('script');
        script.src = 'https://res.wx.qq.com/connect/zh_CN/htmledition/js/wxLogin.js';
        script.async = true;
        document.body.appendChild(script);

        // 接收iframe发来的消息
        const onRecvCode = (e: MessageEvent) => {
            if (e.data.code) {
                const code = e.data.code;
                const state = e.data.state;

                var url = `/api/wechatLogin?code=${code}&state=${encodeURI(state)}`;
                fetch(url)
                    .then((response) => response.json())
                    .then((data) => {
                        console.log(data);
                        setUserInfo(data);
                        setIsLogin(true);
                    });
            }
        }

        // register event listener for window.parent.postMessage in iframe
        window.addEventListener('message', onRecvCode);

        return () => {
            window.removeEventListener('message', onRecvCode);
            document.body.removeChild(script);
        }

    }, []);

    const showWeChatQRCode = async() => {
        setIsLogin(false);
        let data = {
            app_id: 'wx60648d032a743d57',
            redirect_uri: `${window.location.origin}/OnRecvCode`,
            state: '123',
        };

        const response:any = await axios.post('/api/wechatJumpLogin', data);
        console.info(data);
        data = response.data;
        console.info(data);

        // 展示微信二维码
        const wechatLogin = new WxLogin({
            self_redirect: true,
            id: "wechat-login-container",
            appid: "wx60648d032a743d57",
            scope: "snsapi_login",
            redirect_uri: encodeURI(data.redirect_uri),
            state: encodeURI(data.state),
            style: "",
            href: ""
        });
    };

    return (
        <>
            <Button variant='contained' onClick={showWeChatQRCode}>微信登录</Button>
            { isLogin &&
              <div className='user-info'>
                  <h3>用户信息</h3>
                  <img src={userInfo.headimgurl} alt='avatar' />
                  <span>nickname: {userInfo.nickname}</span>
                  <span>openid: {userInfo.openid}</span>
              </div>
            }
            <div id="wechat-login-container" className={isLogin?"hidden":""}></div>
        </>
    )
}