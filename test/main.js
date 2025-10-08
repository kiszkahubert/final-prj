import express from "express";
import QRCode from "qrcode";

const app = express();
const PORT = process.env.PORT || 3000;

app.use(express.urlencoded({ extended: true }));
app.use(express.json());
app.get("/", (req, res) => {
    res.send(`
<!DOCTYPE html>
<html lang="pl">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>QR Generator</title>
  <style>
    body {
      font-family: system-ui, sans-serif;
      background: #f8fafc;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: start;
      padding: 40px;
      color: #1e293b;
    }
    h1 { margin-bottom: 20px; }
    form {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 10px;
    }
    input {
      padding: 10px;
      border: 1px solid #cbd5e1;
      border-radius: 8px;
      width: 280px;
      font-size: 16px;
    }
    button {
      padding: 10px 20px;
      border: none;
      background-color: #2563eb;
      color: white;
      font-weight: 600;
      border-radius: 8px;
      cursor: pointer;
    }
    button:hover { background-color: #1d4ed8; }
    .qr-box {
      margin-top: 30px;
      background: white;
      border: 1px solid #e2e8f0;
      padding: 20px;
      border-radius: 12px;
      box-shadow: 0 4px 12px rgba(0,0,0,0.05);
    }
  </style>
</head>
<body>
  <h1>Generuj QR dla logowania dziecka</h1>
  <form id="form">
    <input id="hashInput" placeholder="Wpisz qrHash..." />
    <button type="submit">Generuj QR</button>
  </form>
  <div id="qrContainer"></div>
  <script type="module">
    const form = document.getElementById("form");
    const input = document.getElementById("hashInput");
    const qrContainer = document.getElementById("qrContainer");
    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      const qrHash = input.value.trim();
      if (!qrHash) {
        alert("Podaj qrHash!");
        return;
      }
      const res = await fetch("/generate", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ qrHash })
      });
      const data = await res.json();
      qrContainer.innerHTML = data.qrUrl
        ? \`<div class="qr-box"><img src="\${data.qrUrl}" alt="QR" /><p>Zeskanuj kod, aby zalogować.</p></div>\`
        : "<p>Błąd generowania QR!</p>";
    });
  </script>
</body>
</html>
  `);
});
app.post("/generate", async (req, res) => {
    const { qrHash } = req.body;
    if (!qrHash) return res.status(400).json({ error: "Brak qrHash" });
    const loginUrl = `https://localhost:8080/auth/qr?hash=${encodeURIComponent(qrHash)}`;
    try {
        const qrUrl = await QRCode.toDataURL(loginUrl);
        res.json({ qrUrl });
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: "Nie udało się wygenerować QR" });
    }
});
app.listen(PORT, () => {
    console.log(`✅ Serwer działa na http://localhost:${PORT}`);
});
