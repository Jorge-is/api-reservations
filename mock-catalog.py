"""
Mock server para api-catalog. Responde a cualquier código de ciudad con datos válidos.
Uso: python mock-catalog.py
"""
from http.server import HTTPServer, BaseHTTPRequestHandler
import json


class CatalogHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        code = self.path.rstrip("/").split("/")[-1].upper()
        body = json.dumps({"name": code, "code": code, "timeZone": "UTC"}).encode()
        self.send_response(200)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def log_message(self, format, *args):
        print(f"  catalog mock → {self.path}")


if __name__ == "__main__":
    server = HTTPServer(("0.0.0.0", 6070), CatalogHandler)
    print("Mock catalog corriendo en http://localhost:6070")
    print("Presiona Ctrl+C para detener\n")
    server.serve_forever()
