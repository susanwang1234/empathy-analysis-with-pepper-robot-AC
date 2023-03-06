import uvicorn
from fastapi import FastAPI, File, UploadFile, Request
import numpy as np
from PIL import Image
from io import BytesIO
from model import QA
from model import get_emotion

app = FastAPI()

@app.get("/", status_code=200)
async def root():
    return {"yey": "asd"}

@app.post("/CV/getLabel")
async def get_label(file: UploadFile):
    image = np.array(Image.open(BytesIO(await file.read())))
    image_str = np.array2string(image, precision=2, separator=',',
                      suppress_small=True)
    return {"label": image_str}

@app.post("/NLP/getResponse")
async def get_response(req: Request):
    body = await req.json()
    answer =  QA(body['question'])
    emotion = get_emotion(body['question'])[6:]
    return {"answer": answer, "emotion": emotion}

if __name__ == "__main__":
    uvicorn.run(app,
        host="0.0.0.0",
        port=8888,
        debug='true')
