from pydantic import BaseModel

class DiffFile(BaseModel):
    filePath: str
    patch: str
    