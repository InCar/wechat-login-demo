import React, { useState, useEffect } from 'react';
import { Button } from '@mui/material';

export const OnCode = () => {
    const [code, setCode] = useState('');
    const [state, setState] = useState('');

    const notifyParent = () => {
        // 通知父窗口收到了code和state
        window.parent.postMessage({code, state}, window.location.origin);
    }

    // get code & state from query string
    useEffect(() => {
        const query = new URLSearchParams(window.location.search);
        setCode(query.get('code')??'');
        setState(query.get('state')??'');

    }, []);

    return (
        <>
            <h3>OnRecvCode</h3>
            <p>
                code:<br />
                <span className='txt-em'>{code}</span><br />
                state:<br />
                <span className='txt-em'>{state}</span>
            </p>
            <Button variant='contained' onClick={notifyParent}>继续</Button>
        </>
    )
}