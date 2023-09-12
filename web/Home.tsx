import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { DateTime } from 'luxon';
import { Login } from './Login';
import { OnCode } from './OnCode';

export const Home = () => {
    const [isOnCode, setIsOnCode] = useState(false);
    const [ver, setVer] = useState<any>('');
    const [tmBuild, setTmBuild] = useState('');

    useEffect(() => {
        (async ()=>{
            const {data} = await axios.get('/api/version');
            console.log(data.version);
            const tm = DateTime.fromMillis(data._tmBuilding);
            setVer(data);
            setTmBuild(tm.setLocale("zh-CN").toLocaleString(DateTime.DATETIME_MED));
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
                    <p>
                        当前版本：<a href={ver.sourceLink} target='_blank'>{ver.version}</a>
                        <br />
                        构建时间：{tmBuild}
                    </p>
                </>
            }
        </>
    )
}