from pydantic import BaseModel

class FullFile(BaseModel):
    filePath: str
    content: str