import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Login } from './Login';
import { OnCode } from './OnCode';

export const Home = () => {
    const [isOnCode, setIsOnCode] = useState(false);

    useEffect(() => {
        (async ()=>{
            const {data} = await axios.get('/api/version');
            console.log(data.version);
        })();

        const path = window.location.pathname;
        setIsOnCode(path === '/OnRecvCode');

    }, []);

    return (
        <>
            { isOnCode ?
                <OnCode /> :
                <>
                    <h1>微信扫码登录示例</h1>
                    <Login />
                </>
            }
        </>
    )
}