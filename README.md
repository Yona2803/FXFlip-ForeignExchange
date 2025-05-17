# FXFlip-ForeignExchange 🍉

FXFlip-ForeignExchange is a modern JavaFX desktop application for real-time currency conversion and exchange rate analysis. It features a clean UI, keyboard-friendly controls, and visual statistics for currency trends.
<br /><br />

---

## 🚀  Features

- **💱 Live Currency Conversion:** Instantly convert between multiple currencies using up-to-date exchange rates.
<br /><br />
- **📊 Historical Data Visualization:** View 14-day exchange rate trends for selected currencies with interactive charts.
<br /><br />
- **📈 Rate Comparison Section:** Quickly compare rates and daily changes for major currencies.
<br /><br />
- **🎨Customizable UI:** Modern, responsive design with keyboard navigation and custom styling.
<br /><br />
- **⚠️ Robust Error Handling:** User-friendly error dialogs for API and network issues.
<br /><br />

---

## 🛠️ Technologies Used

- Java 17+
- JavaFX 17+
- Maven
- [Gson](https://github.com/google/gson) for JSON parsing
- [FastForex API](https://api.fastforex.io) for exchange rates
  <br /><br />

---

## 📦 Getting Started

### ✅ Prerequisites

- Java 17 or newer
- Maven 3.6+
- Internet connection (for live rates)

### 🧪 Build & Run

1. **Clone the repository:**
   ```sh
   git clone https://github.com/YOUR_USERNAME/FXFlip-ForeignExchange.git
   cd FXFlip-ForeignExchange
   ```

2. **Configure API Key:**
   - Edit `src/main/resources/config.properties` and set your `apiKey`.

3. **Build and launch with Maven:**
   ```sh
   mvn clean javafx:run
   ```

   Or build a jar:
   ```sh
   mvn clean package
   java -jar target/FXFlip-ForeignExchange-1.0-SNAPSHOT.jar
   ```
<br />

---

## 📁 Project Structure

- `src/main/java/com/currencyApp/` - Application source code
- `src/main/resources/` - FXML layouts, CSS, Media Asstes and configuration
- `src/test/java/` - Unit tests
  <br /><br />

---

## ⚙️ Configuration

Edit `src/main/resources/config.properties` to set:
- `apiKey` - Your FastForex API key // apiKey=API key
- `baseUrl` - API endpoint // https://api.fastforex.io
- `appName`, `version`, etc.
  <br /><br />

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
<br /><br />

---

👤 **Author:**
Youness Najeh  
📦 Version: 1.0.0
<br />

---

# From the river to the sea, Palestine will be free✌️ #FreePalestine 🍉🇵🇸 