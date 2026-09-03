# FastAPI / Starlette expose WebSockets natively (the `websockets`
# library is an alternative).
from fastapi import FastAPI, WebSocket, WebSocketDisconnect

app = FastAPI()


@app.websocket("/ws")
async def ws_handler(ws: WebSocket):
    # Validate origin yourself before accepting (sec 14), then
    # complete the handshake.
    await ws.accept()  # performs the 101 handshake (sec 4)
    try:
        # the READ LOOP, one coroutine per connection
        while True:
            msg = await ws.receive_text()  # awaits the next message
            await ws.send_text(msg)  # echo it straight back
    except WebSocketDisconnect:
        # client closed or connection died (sec 6)
        pass
    # FastAPI cleans up the connection when the coroutine returns

# uvicorn app:app, uvicorn speaks the WebSocket protocol for you
