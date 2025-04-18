# 🔁 Binance Futures Signal Broadcaster & Mirror Integration

**Empower your Binance futures trading with real-time transparency and seamless signal forwarding.**  
Whether you're a **trader** managing accounts or an **investor** following pro signals — this open-source project is for you.

![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)
![Build](https://img.shields.io/badge/build-passing-brightgreen)
![Docker](https://img.shields.io/badge/docker-ready-blue)
![Binance](https://img.shields.io/badge/binance-futures-yellow)

---

## 💡 Features

- 📤 **Trade Action Broadcasting**  
  Forwards key Binance Futures trading actions to your **Telegram channel**, including:
  - New **LIMIT** or **MARKET** orders
  - **Stop Loss** / **Take Profit** updates
  - **Position adjustments**, **order moves**, **cancellations**
  - **Partial** or **complete closes**

- 🔄 **Queue Integration**  
  Sends every event to a **message queue** (`ActiveMQ`, will add `Kafka` later) for:
  - Third-party integration (e.g., trade copier systems)
  - Custom dashboards or analytics
  - Audit logs and downstream processing

- 👁️ **Investor View / Mirror Monitoring**  
  Enables **investors** to track all trades in real-time, including:
  - Current open positions
  - Live SL/TP levels
  - Position sizes and direction


## ⚠️ Privacy Note

By default, the API exposes real balance and position size.  
If you're using this app to share signals publicly, you can **customize the JSON response** to hide sensitive information and show only relative values (like % of equity or margin).

---

## 📌 Use Cases

| Role         | Benefit                                                                 |
|--------------|-------------------------------------------------------------------------|
| **Trader**   | Broadcast trade updates to followers instantly                          |
| **Investor** | Monitor managed accounts live, including positions and SL/TP info       |
| **Dev Team** | Integrate signals into a copier, analytics system, or Telegram bot      |

---

## 💬 Contribute

Got ideas? Found a bug? Want to improve the code?  
Feel free to [open an issue](https://github.com/danielkniaz/prada/issues) or submit a pull request. Contributions are welcome!

---

## 📄 License

This project is licensed under the MIT License. See [LICENSE](https://github.com/danielkniaz/prada/blob/main/LICENSE) for details.

---


Stay transparent. Stay connected.  
**Happy trading 🚀**


---

## 🚀 Getting Started

### 1. Configure the App

Edit the `application.yml` file:

```yaml
telegram:
  enabled: true
  botToken: YOUR_BOT_TOKEN
  chatId: YOUR_CHAT_ID

binance:
  apiKey: YOUR_BINANCE_API_KEY
  secretKey: YOUR_BINANCE_SECRET_KEY

queue:
  enabled: true
  type: kafka  # or amq
  url: YOUR_QUEUE_URL

database:
  url: jdbc:postgresql://localhost:5432/your_db
  username: your_user
  password: your_pass```

### 2. Build and deploy

mvn clean install
docker build -t binance-signal-broadcaster .
docker run -d -p 8080:8080 binance-signal-broadcaster


