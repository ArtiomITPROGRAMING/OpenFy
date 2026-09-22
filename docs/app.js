/*
 * OpenFy Themes Hub - Interactive Client Application with Auth & Advanced Theme Studio
 * Copyright (C) 2026 ArtiomITPROGRAMING - GNU GPL v3
 */

// Official Theme Catalog Data
const THEMES_DATA = [
  {
    id: "emerald-matrix",
    name: "Изумрудная Матрица",
    nameEn: "Cyber Emerald",
    author: "OpenFy Team",
    version: "1.0.0",
    category: "cyberpunk",
    categoryLabel: "Киберпанк & Неон",
    description: "Кибернетический изумрудный терминал в стиле научной фантастики, матричного кода и хакерской эстетики с максимальным контрастом.",
    downloadUrl: "https://raw.githubusercontent.com/ArtiomITPROGRAMING/OpenFy/main/themes/emerald-matrix.thm",
    rawJsonUrl: "https://raw.githubusercontent.com/ArtiomITPROGRAMING/OpenFy/main/themes/emerald-matrix/theme.json",
    backgroundStyle: "CYBER_GRID",
    fontFamily: "MONO",
    iconStyle: "CYBER_SHARP",
    playerLayout: "VINYL_DISC",
    colors: {
      primary: "#00FF66",
      onPrimary: "#000000",
      secondary: "#00E5FF",
      background: "#050B07",
      surface: "#0C1810",
      surfaceVariant: "#152419",
      onSurface: "#E0F5E6",
      onSurfaceVariant: "#7FA388",
      accent: "#00FF66",
      card: "#0F2115",
      glow: "rgba(0, 255, 102, 0.45)"
    },
    baseRating: 4.95,
    baseRatingCount: 142,
    baseLikes: 384
  },
  {
    id: "nordic-frost",
    name: "Северное Сияние",
    nameEn: "Nordic Frost",
    author: "OpenFy Team",
    version: "1.0.0",
    category: "chill",
    categoryLabel: "Северный чилл",
    description: "Холодная арктическая эстетика с мерцанием полярного сияния, глубоким сапфировым фоном и акцентами цвета ледяного кристалла.",
    downloadUrl: "https://raw.githubusercontent.com/ArtiomITPROGRAMING/OpenFy/main/themes/nordic-frost.thm",
    rawJsonUrl: "https://raw.githubusercontent.com/ArtiomITPROGRAMING/OpenFy/main/themes/nordic-frost/theme.json",
    backgroundStyle: "AURORA_MESH",
    fontFamily: "INTER",
    iconStyle: "ROUNDED",
    playerLayout: "ALBUM_CARD",
    colors: {
      primary: "#38BDF8",
      onPrimary: "#001E2E",
      secondary: "#A855F7",
      background: "#080E1A",
      surface: "#111A2E",
      surfaceVariant: "#1C273E",
      onSurface: "#E2E8F0",
      onSurfaceVariant: "#94A3B8",
      accent: "#38BDF8",
      card: "#131D33",
      glow: "rgba(56, 189, 248, 0.45)"
    },
    baseRating: 4.88,
    baseRatingCount: 98,
    baseLikes: 251
  },
  {
    id: "sunset-synthwave",
    name: "Закатный Синтвейв",
    nameEn: "Sunset Synthwave 80s",
    author: "OpenFy Team",
    version: "1.0.0",
    category: "retro",
    categoryLabel: "Ретровейв 80-х",
    description: "Атмосфера Майами 80-х: неоновый закат, яркие оранжевые и фуксия градиенты с тёмным фиолетовым ночным фоном шоссе.",
    downloadUrl: "https://raw.githubusercontent.com/ArtiomITPROGRAMING/OpenFy/main/themes/sunset-synthwave.thm",
    rawJsonUrl: "https://raw.githubusercontent.com/ArtiomITPROGRAMING/OpenFy/main/themes/sunset-synthwave/theme.json",
    backgroundStyle: "RADIAL_GLOW",
    fontFamily: "ORBITRON",
    iconStyle: "NEON_GLOW",
    playerLayout: "VINYL_DISC",
    colors: {
      primary: "#FF7A00",
      onPrimary: "#000000",
      secondary: "#FF007A",
      background: "#12091A",
      surface: "#1D102A",
      surfaceVariant: "#2B1A3B",
      onSurface: "#FFE6F0",
      onSurfaceVariant: "#C49BB5",
      accent: "#FF007A",
      card: "#20122E",
      glow: "rgba(255, 122, 0, 0.45)"
    },
    baseRating: 4.92,
    baseRatingCount: 164,
    baseLikes: 419
  },
  {
    id: "tokyo-night",
    name: "Токийская Ночь",
    nameEn: "Tokyo Night & Sakura",
    author: "OpenFy Team",
    version: "1.0.0",
    category: "tokyo",
    categoryLabel: "Токио Неон",
    description: "Пастельная сакура и неоновые огни ночного квартала Сибуя: мягкий розовый и деликатный индиго на глубоком асфальтовом фоне.",
    downloadUrl: "https://raw.githubusercontent.com/ArtiomITPROGRAMING/OpenFy/main/themes/tokyo-night.thm",
    rawJsonUrl: "https://raw.githubusercontent.com/ArtiomITPROGRAMING/OpenFy/main/themes/tokyo-night/theme.json",
    backgroundStyle: "LINEAR_GRADIENT",
    fontFamily: "OUTFIT",
    iconStyle: "MINIMAL_LINE",
    playerLayout: "ALBUM_CARD",
    colors: {
      primary: "#F43F5E",
      onPrimary: "#FFFFFF",
      secondary: "#818CF8",
      background: "#090A10",
      surface: "#121422",
      surfaceVariant: "#1C1F33",
      onSurface: "#F1F5F9",
      onSurfaceVariant: "#8C96AD",
      accent: "#818CF8",
      card: "#151829",
      glow: "rgba(244, 63, 94, 0.45)"
    },
    baseRating: 4.91,
    baseRatingCount: 112,
    baseLikes: 305
  },
  {
    id: "pure-gold-luxury",
    name: "Королевский Оникс",
    nameEn: "Obsidian & Royal Gold",
    author: "OpenFy Team",
    version: "1.0.0",
    category: "luxury",
    categoryLabel: "Королевский люкс",
    description: "Премиальный глубокий обсидиановый оникс в сочетании с благородным 24k золотом и тёплыми янтарными акцентами для ценителей роскоши.",
    downloadUrl: "https://raw.githubusercontent.com/ArtiomITPROGRAMING/OpenFy/main/themes/pure-gold-luxury.thm",
    rawJsonUrl: "https://raw.githubusercontent.com/ArtiomITPROGRAMING/OpenFy/main/themes/pure-gold-luxury/theme.json",
    backgroundStyle: "SOLID",
    fontFamily: "PLAYFAIR",
    iconStyle: "ROUNDED",
    playerLayout: "VINYL_DISC",
    colors: {
      primary: "#FFD700",
      onPrimary: "#000000",
      secondary: "#FFA000",
      background: "#0C0A05",
      surface: "#19150B",
      surfaceVariant: "#2B2414",
      onSurface: "#FFF8E7",
      onSurfaceVariant: "#BDB08E",
      accent: "#FFD700",
      card: "#1F1A0E",
      glow: "rgba(255, 215, 0, 0.45)"
    },
    baseRating: 4.97,
    baseRatingCount: 189,
    baseLikes: 532
  }
];

// App Global State
let currentPreviewTheme = THEMES_DATA[0];
let activeCategory = "all";
let searchQuery = "";
let isMockupPlaying = true;
let currentMockupTab = "player"; // 'player' | 'library' | 'studio'

// Studio Customization State
let studioState = {
  name: "Моя Студийная Тема",
  author: "OpenFy Creator",
  version: "1.0.0",
  primary: "#00FF66",
  secondary: "#00E5FF",
  background: "#050B07",
  surface: "#0C1810",
  onSurface: "#E0F5E6",
  backgroundStyle: "CYBER_GRID", // SOLID, LINEAR_GRADIENT, RADIAL_GLOW, CYBER_GRID, AURORA_MESH
  fontFamily: "OUTFIT",          // OUTFIT, INTER, MONO, ORBITRON, PLAYFAIR
  iconStyle: "ROUNDED",          // ROUNDED, CYBER_SHARP, MINIMAL_LINE, NEON_GLOW
  playerLayout: "VINYL_DISC"     // VINYL_DISC, ALBUM_CARD
};

// Local Storage Helper
const storage = {
  getRatings() {
    try {
      return JSON.parse(localStorage.getItem("openfy_theme_ratings") || "{}");
    } catch { return {}; }
  },
  saveRating(id, score) {
    const data = this.getRatings();
    data[id] = score;
    localStorage.setItem("openfy_theme_ratings", JSON.stringify(data));
  },
  getLikes() {
    try {
      return JSON.parse(localStorage.getItem("openfy_theme_likes") || "{}");
    } catch { return {}; }
  },
  toggleLike(id) {
    const data = this.getLikes();
    data[id] = !data[id];
    localStorage.setItem("openfy_theme_likes", JSON.stringify(data));
    return data[id];
  },
  getUser() {
    try {
      return JSON.parse(localStorage.getItem("openfy_auth_user") || "null");
    } catch { return null; }
  },
  saveUser(user) {
    localStorage.setItem("openfy_auth_user", JSON.stringify(user));
  },
  clearUser() {
    localStorage.removeItem("openfy_auth_user");
  }
};

// ============================================================================
// ============================================================================
// Auth & Max Security 2FA Match Engine
// ============================================================================
const authEngine = {
  currentUser: null,
  pendingAppUser: null,
  pending2FaChallenge: null,
  timerInterval: null,

  init() {
    this.currentUser = storage.getUser();
    this.checkUrlSyncParams();
    this.renderHeaderUserWidget();
  },

  checkUrlSyncParams() {
    const params = new URLSearchParams(window.location.search);
    if (params.get("auth_sync") === "1") {
      const incomingUser = {
        username: params.get("user") || "OpenFy User",
        provider: params.get("provider") || "openfy_sync",
        avatarUrl: params.get("avatar") || "",
        id: params.get("id") || `sync_${Date.now()}`,
        syncedWithApp: true,
        securityStatus: "PENDING_2FA_VERIFICATION"
      };

      const ip = params.get("ip") || "";
      const port = params.get("port") || "8888";
      const secCode = params.get("sec_code") || "";

      if (ip) {
        wifiSyncEngine.savePhoneAddress(ip, port);
      }

      // Clean query parameters from URL for security
      const cleanUrl = window.location.origin + window.location.pathname;
      window.history.replaceState({}, document.title, cleanUrl);

      // Check for account mismatch
      if (this.currentUser && this.currentUser.username.toLowerCase() !== incomingUser.username.toLowerCase()) {
        this.pendingAppUser = incomingUser;
        this.showMismatchBanner(this.currentUser, incomingUser);
        return;
      }

      // Max Security 2FA Challenge Trigger
      this.trigger2FaSecurityChallenge(incomingUser, secCode, ip, port);
    }
  },

  wifiPollInterval: null,

  trigger2FaSecurityChallenge(incomingUser, secCode, ip, port) {
    const code = secCode || Math.floor(100000 + Math.random() * 900000).toString();
    const effectiveIp = ip || (typeof wifiSyncEngine !== "undefined" && wifiSyncEngine.ip ? wifiSyncEngine.ip : "");
    const effectivePort = port || (typeof wifiSyncEngine !== "undefined" && wifiSyncEngine.port ? wifiSyncEngine.port : "8888");

    this.pending2FaChallenge = {
      user: incomingUser,
      code: code,
      ip: effectiveIp,
      port: effectivePort,
      expiresAt: Date.now() + 120 * 1000 // 120s
    };

    // 1. Render 6 digits in PIN display
    const digits = code.padStart(6, "0").split("");
    digits.forEach((d, i) => {
      const cell = document.getElementById(`pin-${i}`);
      if (cell) cell.textContent = d;
    });

    // 2. Set username in modal
    const userSpan = document.getElementById("sec-2fa-username");
    if (userSpan) userSpan.textContent = `@${incomingUser.username}`;

    const ipSpan = document.getElementById("sec-device-ip");
    if (ipSpan) ipSpan.textContent = effectiveIp ? `Wi-Fi: ${effectiveIp}:${effectivePort}` : "Локальная сеть / P2P";

    // 3. Configure Direct Deep Link Button for Mobile (Instant Open in OpenFy)
    const deepLink = `openfy://auth?user=${encodeURIComponent(incomingUser.username)}&code=${code}`;
    const deepLinkBtn = document.getElementById("sec-deeplink-btn");
    if (deepLinkBtn) deepLinkBtn.href = deepLink;

    // 4. Populate Wi-Fi IP input
    const ipInput = document.getElementById("sec-wifi-ip-input");
    if (ipInput) ipInput.value = effectiveIp;

    // 5. Generate and render QR code for OpenFy camera scanner
    const qrContainer = document.getElementById("sec-qr-container");
    if (qrContainer && typeof generateSvgQrCode === "function") {
      qrContainer.innerHTML = generateSvgQrCode(deepLink);
    }

    // 6. Reset manual PIN input & status indicators
    const manualPinInput = document.getElementById("sec-user-pin-input");
    if (manualPinInput) manualPinInput.value = "";

    const pushStatus = document.getElementById("sec-wifi-push-status");
    if (pushStatus) {
      pushStatus.style.display = "none";
      pushStatus.className = "sec-status-msg";
      pushStatus.textContent = "";
    }

    const autoStatusText = document.getElementById("sec-auto-status-text");
    if (autoStatusText) {
      autoStatusText.textContent = effectiveIp 
        ? `Отправка запроса на ${effectiveIp}...` 
        : "Ожидание подтверждения на смартфоне...";
    }

    // 7. Start timer & open modal
    this.start2FaTimer();
    open2FaModal();

    // 8. If phone IP is already configured, automatically push challenge & start polling!
    if (effectiveIp) {
      this.sendChallengeToPhoneWifi(false);
    }
  },

  async sendChallengeToPhoneWifi(showNotification = true) {
    if (!this.pending2FaChallenge) return;
    const challenge = this.pending2FaChallenge;
    const ipInput = document.getElementById("sec-wifi-ip-input");
    const ip = (ipInput ? ipInput.value.trim() : "") || challenge.ip || (typeof wifiSyncEngine !== "undefined" ? wifiSyncEngine.ip : "");
    const port = challenge.port || "8888";

    const pushStatus = document.getElementById("sec-wifi-push-status");
    const autoStatusText = document.getElementById("sec-auto-status-text");

    if (!ip) {
      if (pushStatus) {
        pushStatus.style.display = "block";
        pushStatus.className = "sec-status-msg error";
        pushStatus.textContent = "⚠️ Укажите IP-адрес смартфона в вашей сети Wi-Fi (напр. 192.168.1.55)";
      }
      if (showNotification) showToast("Укажите IP-адрес смартфона");
      return;
    }

    challenge.ip = ip;
    if (typeof wifiSyncEngine !== "undefined" && wifiSyncEngine.savePhoneAddress) {
      wifiSyncEngine.savePhoneAddress(ip, port);
    }

    if (pushStatus) {
      pushStatus.style.display = "block";
      pushStatus.className = "sec-status-msg";
      pushStatus.style.background = "rgba(0, 229, 255, 0.1)";
      pushStatus.style.color = "#00E5FF";
      pushStatus.style.border = "1px solid rgba(0, 229, 255, 0.3)";
      pushStatus.textContent = `📡 Отправка запроса на ${ip}:${port}...`;
    }

    try {
      const res = await fetch(`http://${ip}:${port}/api/auth/request_challenge`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username: challenge.user.username, code: challenge.code }),
        signal: AbortSignal.timeout(3500)
      });

      if (res.ok) {
        const data = await res.json();
        // If server provided code, synchronize it
        if (data.code && data.code.length === 6 && data.code !== challenge.code) {
          challenge.code = data.code;
          const digits = data.code.split("");
          digits.forEach((d, i) => {
            const cell = document.getElementById(`pin-${i}`);
            if (cell) cell.textContent = d;
          });
        }

        if (pushStatus) {
          pushStatus.style.display = "block";
          pushStatus.className = "sec-status-msg success";
          pushStatus.textContent = `✅ Запрос передан в OpenFy на ${ip}! Нажмите «Подтвердить» на экране телефона.`;
        }
        if (autoStatusText) {
          autoStatusText.textContent = `Запрос передан на ${ip}! Нажмите «Подтвердить» в приложении.`;
        }
        if (showNotification) showToast(`✅ Запрос отправлен в OpenFy на телефоне (${ip})`);
        
        // Start polling for approval from phone
        this.startWifiAuthPolling(ip, port);
        return;
      }
    } catch (_) {}

    if (pushStatus) {
      pushStatus.style.display = "block";
      pushStatus.className = "sec-status-msg error";
      pushStatus.textContent = `⚠️ Телефон ${ip} не ответил. Убедитесь, что OpenFy запущен на телефоне в той же сети Wi-Fi, либо нажмите «Открыть в OpenFy» выше.`;
    }
    if (autoStatusText) {
      autoStatusText.textContent = "Ожидание подтверждения на смартфоне...";
    }
    if (showNotification) showToast(`Не удалось связаться с ${ip}:${port}`);
  },

  startWifiAuthPolling(ip, port) {
    this.stopWifiAuthPolling();
    const effectiveIp = ip || (this.pending2FaChallenge ? this.pending2FaChallenge.ip : "");
    const effectivePort = port || "8888";
    if (!effectiveIp) return;

    this.wifiPollInterval = setInterval(async () => {
      if (!this.pending2FaChallenge) {
        this.stopWifiAuthPolling();
        return;
      }

      try {
        const res = await fetch(`http://${effectiveIp}:${effectivePort}/api/auth/status`, {
          method: "GET",
          signal: AbortSignal.timeout(2000)
        });
        if (res.ok) {
          const data = await res.json();
          if (data.approved === true) {
            this.stopWifiAuthPolling();
            showToast("🛡️ Вход подтвержден в приложении на телефоне!");
            this.confirm2FaMatch();
          }
        }
      } catch (_) {}
    }, 1500);
  },

  stopWifiAuthPolling() {
    if (this.wifiPollInterval) {
      clearInterval(this.wifiPollInterval);
      this.wifiPollInterval = null;
    }
  },

  toggleQrCodeSection() {
    const sec = document.getElementById("sec-qr-section");
    const label = document.getElementById("sec-qr-toggle-label");
    const qrContainer = document.getElementById("sec-qr-container");
    if (!sec) return;
    const isHidden = sec.style.display === "none";
    sec.style.display = isHidden ? "block" : "none";
    if (label) label.textContent = isHidden ? "Скрыть QR ▲" : "Показать QR ▼";
    if (isHidden && qrContainer && this.pending2FaChallenge) {
      const deepLink = `openfy://auth?user=${encodeURIComponent(this.pending2FaChallenge.user.username)}&code=${this.pending2FaChallenge.code}`;
      qrContainer.innerHTML = generateSvgQrCode(deepLink);
    }
  },

  onManualPinInput(val) {
    if (!this.pending2FaChallenge) return;
    const clean = (val || "").replace(/\D/g, "");
    if (clean.length === 6) {
      if (clean === this.pending2FaChallenge.code) {
        showToast("✅ 6-значный код безопасности совпал!");
        this.confirm2FaMatch();
      } else {
        showToast("❌ Код не совпадает с кодом из приложения");
      }
    }
  },

  start2FaTimer() {
    if (this.timerInterval) clearInterval(this.timerInterval);
    const timerText = document.getElementById("sec-timer-text");
    const timerProgress = document.getElementById("sec-timer-progress");

    const updateTimer = () => {
      if (!this.pending2FaChallenge) {
        clearInterval(this.timerInterval);
        return;
      }
      const remainingMs = this.pending2FaChallenge.expiresAt - Date.now();
      if (remainingMs <= 0) {
        clearInterval(this.timerInterval);
        if (timerText) timerText.textContent = "00:00 (Истёк)";
        if (timerProgress) timerProgress.style.width = "0%";
        showToast("⚠️ Время действия 2FA кода безопасности истекло");
        this.reject2Fa();
        return;
      }

      const totalSec = Math.floor(remainingMs / 1000);
      const minutes = Math.floor(totalSec / 60).toString().padStart(2, "0");
      const seconds = (totalSec % 60).toString().padStart(2, "0");
      if (timerText) timerText.textContent = `${minutes}:${seconds}`;
      if (timerProgress) {
        const percent = Math.max(0, (remainingMs / 120000) * 100);
        timerProgress.style.width = `${percent}%`;
      }
    };

    updateTimer();
    this.timerInterval = setInterval(updateTimer, 1000);
  },

  async confirm2FaMatch() {
    if (!this.pending2FaChallenge) return;
    const challenge = this.pending2FaChallenge;

    this.stopWifiAuthPolling();

    // Mutual Wi-Fi verification handshake if reachable
    if (challenge.ip) {
      try {
        await fetch(`http://${challenge.ip}:${challenge.port}/api/auth/verify`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ code: challenge.code }),
          signal: AbortSignal.timeout(2000)
        });
      } catch (_) {}
    }

    clearInterval(this.timerInterval);
    const user = challenge.user;
    user.is2FaVerified = true;
    user.securityLevel = "MAXIMUM_2FA_PIN_PAIRED";
    user.verifiedAt = new Date().toISOString();
    user.sessionToken = "sec_" + Math.random().toString(36).substring(2) + Date.now().toString(36);

    this.currentUser = user;
    storage.saveUser(user);
    this.pending2FaChallenge = null;

    if (studioState) {
      studioState.author = user.username;
      const authorInput = document.getElementById("creator-author");
      if (authorInput) authorInput.value = user.username;
    }

    this.renderHeaderUserWidget();
    close2FaModal();
    showToast(`🛡️ Вход подтвержден: 2FA код совпал! Добро пожаловать, @${user.username}`);
  },

  reject2Fa() {
    clearInterval(this.timerInterval);
    this.stopWifiAuthPolling();
    this.pending2FaChallenge = null;
    close2FaModal();
    showToast("Вход в аккаунт отклонён в целях безопасности.");
  },

  showMismatchBanner(webUser, appUser) {
    const banner = document.getElementById("account-mismatch-banner");
    if (!banner) return;

    banner.innerHTML = `
      <div>
        ⚠️ <strong>Несовпадение аккаунтов:</strong> на сайте активен <b>@${webUser.username}</b>, а в приложении OpenFy на телефоне <b>@${appUser.username}</b>.
      </div>
      <div class="mismatch-actions">
        <button class="btn-mismatch-sync" onclick="authEngine.applyAppSync()">Синхронизировать с телефоном</button>
        <button class="btn-mismatch-dismiss" onclick="authEngine.dismissMismatchBanner()">Оставить @${webUser.username}</button>
      </div>
    `;
    banner.style.display = "flex";
  },

  applyAppSync() {
    if (this.pendingAppUser) {
      const userToSync = this.pendingAppUser;
      this.pendingAppUser = null;
      this.dismissMismatchBanner();
      this.trigger2FaSecurityChallenge(userToSync, null, null, null);
    }
  },

  dismissMismatchBanner() {
    const banner = document.getElementById("account-mismatch-banner");
    if (banner) banner.style.display = "none";
  },

  renderHeaderUserWidget() {
    const container = document.getElementById("user-auth-widget");
    if (!container) return;

    if (!this.currentUser) {
      container.innerHTML = `
        <button class="btn-github" onclick="openAuthModal()" style="font-size: 0.8rem; padding: 6px 14px;">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm0 14.2c-2.5 0-4.71-1.28-6-3.22.03-1.99 4-3.08 6-3.08 1.99 0 5.97 1.09 6 3.08-1.29 1.94-3.5 3.22-6 3.22z"/></svg>
          Войти
        </button>
      `;
    } else {
      const avatarInitial = this.currentUser.username.charAt(0).toUpperCase();
      const avatarHtml = this.currentUser.avatarUrl ? 
        `<img src="${this.currentUser.avatarUrl}" alt="${this.currentUser.username}" onerror="this.onerror=null; this.parentNode.textContent='${avatarInitial}';"/>` :
        avatarInitial;

      const providerBadge = this.currentUser.is2FaVerified ? "2FA Защищён" : (this.currentUser.syncedWithApp ? "OpenFy Sync" : (this.currentUser.provider || "Web"));

      container.innerHTML = `
        <button class="user-profile-btn" onclick="authEngine.toggleUserDropdown()">
          <div class="user-avatar-circle">${avatarHtml}</div>
          <span>@${this.currentUser.username}</span>
          <span class="badge-pill active" style="font-size: 0.65rem; padding: 2px 6px;">${providerBadge}</span>
          <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor"><path d="M7 10l5 5 5-5z"/></svg>
        </button>

        <div class="user-dropdown-menu" id="user-dropdown-menu">
          <div class="user-dropdown-header">
            <strong>${this.currentUser.username}</strong>
            <div>${providerBadge} • Создатель тем</div>
          </div>
          <button class="user-dropdown-item" onclick="openAuthModal()">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M12 4V1L8 5l4 4V6c3.31 0 6 2.69 6 6 0 1.01-.25 1.97-.7 2.8l1.46 1.46C19.54 15.03 20 13.57 20 12c0-4.42-3.58-8-8-8zm0 14c-3.31 0-6-2.69-6-6 0-1.01.25-1.97.7-2.8L5.24 7.74C4.46 8.97 4 10.43 4 12c0 4.42 3.58 8 8 8v3l4-4-4-4v3z"/></svg>
            Переключить аккаунт
          </button>
          <button class="user-dropdown-item" onclick="authEngine.logout()" style="color: #F43F5E;">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M17 7l-1.41 1.41L18.17 11H8v2h10.17l-2.58 2.58L17 17l5-5zM4 5h8V3H4c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h8v-2H4V5z"/></svg>
            Выйти
          </button>
        </div>
      `;
    }
  },

  toggleUserDropdown() {
    const menu = document.getElementById("user-dropdown-menu");
    if (menu) menu.classList.toggle("show");
  },

  loginAsGuest(nickname) {
    const nick = (nickname || "").trim() || "OpenFy Creator";
    const user = {
      username: nick,
      provider: "guest",
      avatarUrl: "",
      id: `guest_${Date.now()}`,
      syncedWithApp: false
    };
    closeAuthModal();
    this.trigger2FaSecurityChallenge(user, null, null, null);
  },

  loginWithGitHub(tokenOrUsername) {
    const val = (tokenOrUsername || "").trim();
    if (!val) {
      alert("Пожалуйста, введите ваш никнейм GitHub или Personal Access Token");
      return;
    }

    const username = val.replace(/^@/, "");
    const user = {
      username: username,
      provider: "github",
      avatarUrl: `https://github.com/${username}.png`,
      id: `gh_${username}`,
      syncedWithApp: false
    };
    closeAuthModal();
    this.trigger2FaSecurityChallenge(user, null, null, null);
  },

  loginWithDiscord(tag) {
    const val = (tag || "").trim() || "DiscordCreator";
    const user = {
      username: val,
      provider: "discord",
      avatarUrl: "",
      id: `dc_${Date.now()}`,
      syncedWithApp: false
    };
    closeAuthModal();
    this.trigger2FaSecurityChallenge(user, null, null, null);
  },

  logout() {
    storage.clearUser();
    this.currentUser = null;
    this.renderHeaderUserWidget();
    showToast("Вы вышли из профиля");
  }
};

// Close dropdown on click outside
document.addEventListener("click", (e) => {
  if (!e.target.closest(".user-auth-widget")) {
    const menu = document.getElementById("user-dropdown-menu");
    if (menu) menu.classList.remove("show");
  }
});

// ============================================================================
// Wi-Fi Local P2P Sync Engine
// ============================================================================
const wifiSyncEngine = {
  ip: localStorage.getItem("openfy_wifi_ip") || "",
  port: localStorage.getItem("openfy_wifi_port") || "8888",
  isOnline: false,

  init() {
    this.updateStatusPill();
    if (this.ip) {
      this.pingPlayer(this.ip, false);
    }
  },

  savePhoneAddress(ip, port = "8888") {
    if (!ip) return;
    this.ip = ip.trim();
    this.port = port || "8888";
    localStorage.setItem("openfy_wifi_ip", this.ip);
    localStorage.setItem("openfy_wifi_port", this.port);
    this.pingPlayer(this.ip, false);
  },

  saveManualIp() {
    const input = document.getElementById("wifi-ip-input");
    if (!input || !input.value.trim()) {
      showToast("Укажите корректный IP-адрес");
      return;
    }
    this.savePhoneAddress(input.value.trim());
    closeWifiModal();
    showToast(`IP-адрес сохранён: ${this.ip}`);
  },

  async pingPlayer(ipToCheck, notify = true) {
    const ip = ipToCheck || this.ip;
    if (!ip) {
      if (notify) showToast("Укажите IP-адрес для проверки");
      return;
    }

    const cardDot = document.getElementById("wifi-card-dot");
    const cardStatusText = document.getElementById("wifi-card-status-text");
    if (cardStatusText) cardStatusText.textContent = "Проверка связи...";

    try {
      const res = await fetch(`http://${ip}:${this.port}/api/status`, {
        method: "GET",
        mode: "cors",
        signal: AbortSignal.timeout(3000)
      });
      if (res.ok) {
        const data = await res.json();
        this.isOnline = true;
        this.ip = ip;
        localStorage.setItem("openfy_wifi_ip", ip);
        this.updateStatusPill();
        if (cardDot) cardDot.className = "wifi-pulse-dot online";
        if (cardStatusText) cardStatusText.textContent = `В сети (${data.device || "OpenFy"})`;
        if (notify) showToast(`📱 Плеер найден в сети: ${ip}:${this.port}`);
        return true;
      }
    } catch (_) {}

    this.isOnline = false;
    this.updateStatusPill();
    if (cardDot) cardDot.className = "wifi-pulse-dot";
    if (cardStatusText) cardStatusText.textContent = "Не найден (Офлайн)";
    if (notify) showToast(`Не удалось подключиться к ${ip}:${this.port}`);
    return false;
  },

  updateStatusPill() {
    const pill = document.getElementById("wifi-status-pill");
    const dot = document.getElementById("wifi-dot");
    const text = document.getElementById("wifi-status-text");
    if (!pill || !dot || !text) return;

    if (this.isOnline) {
      pill.classList.add("connected");
      dot.className = "wifi-pulse-dot online";
      text.textContent = "Wi-Fi: Онлайн";
    } else if (this.ip) {
      pill.classList.remove("connected");
      dot.className = "wifi-pulse-dot";
      text.textContent = `Wi-Fi: ${this.ip}`;
    } else {
      pill.classList.remove("connected");
      dot.className = "wifi-pulse-dot";
      text.textContent = "Wi-Fi";
    }
  },

  async applyThemeViaWifi(themeId) {
    const theme = THEMES_DATA.find(t => t.id === themeId);
    if (!theme) return;

    if (!this.ip) {
      openWifiModal();
      showToast("Укажите IP-адрес плеера в сети Wi-Fi");
      return;
    }

    const payload = {
      id: theme.id,
      name: theme.name,
      author: theme.author,
      version: theme.version || "1.0.0",
      isDark: true,
      backgroundStyle: theme.backgroundStyle,
      fontFamily: theme.fontFamily,
      iconStyle: theme.iconStyle,
      playerLayout: theme.playerLayout,
      creatorProfile: authEngine.currentUser ? {
        username: authEngine.currentUser.username,
        provider: authEngine.currentUser.provider,
        avatarUrl: authEngine.currentUser.avatarUrl
      } : null,
      colors: {
        primary: theme.colors.primary,
        onPrimary: theme.colors.onPrimary || "#000000",
        secondary: theme.colors.secondary,
        background: theme.colors.background,
        surface: theme.colors.surface,
        surfaceVariant: theme.colors.surfaceVariant || theme.colors.surface,
        onSurface: theme.colors.onSurface,
        onSurfaceVariant: theme.colors.onSurfaceVariant || "#8C96AD",
        accent: theme.colors.accent || theme.colors.primary,
        cardColor: theme.colors.card || theme.colors.surface
      }
    };

    await this.sendThemePayload(payload);
  },

  async applyStudioThemeViaWifi() {
    if (!this.ip) {
      openWifiModal();
      showToast("Укажите IP-адрес плеера в сети Wi-Fi");
      return;
    }
    const payload = generateThemeJsonContent();
    await this.sendThemePayload(payload);
  },

  async sendThemePayload(payload) {
    showToast(`⚡ Отправка темы «${payload.name}» в OpenFy по Wi-Fi...`);

    try {
      const res = await fetch(`http://${this.ip}:${this.port}/api/theme/apply`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
        mode: "cors",
        signal: AbortSignal.timeout(5000)
      });

      if (res.ok) {
        this.isOnline = true;
        this.updateStatusPill();
        showToast(`🎉 Тема «${payload.name}» успешно применена в OpenFy!`);
      } else {
        showToast(`Ошибка плеера при применении темы (код: ${res.status})`);
      }
    } catch (err) {
      showToast(`⚠️ Не удалось связаться с ${this.ip}:${this.port}. Проверьте Wi-Fi в плеере.`);
      openWifiModal();
    }
  }
};

// ============================================================================
// DOM Init
// ============================================================================
document.addEventListener("DOMContentLoaded", () => {
  authEngine.init();
  wifiSyncEngine.init();
  renderThemeCards();
  setupFilters();
  setupSearch();
  setupMockupControls();
  setupThemeStudio();
  applyThemeToMockup(currentPreviewTheme);
  startWaveformAnimation();
});

// Render Themes
function renderThemeCards() {
  const container = document.getElementById("themes-container");
  if (!container) return;

  const userRatings = storage.getRatings();
  const userLikes = storage.getLikes();

  const filtered = THEMES_DATA.filter(theme => {
    const matchesCat = activeCategory === "all" || theme.category === activeCategory;
    const query = searchQuery.toLowerCase().trim();
    const matchesSearch = !query || 
      theme.name.toLowerCase().includes(query) || 
      theme.nameEn.toLowerCase().includes(query) || 
      theme.description.toLowerCase().includes(query) ||
      theme.author.toLowerCase().includes(query);
    return matchesCat && matchesSearch;
  });

  if (filtered.length === 0) {
    container.innerHTML = `
      <div style="text-align: center; padding: 3rem; background: var(--card-bg); border-radius: var(--radius-lg); border: 1px solid var(--card-border);">
        <p style="color: var(--text-muted); font-size: 1.1rem;">Темы по вашему запросу не найдены.</p>
        <button class="btn btn-secondary" style="margin-top: 1rem;" onclick="resetSearch()">Сбросить поиск</button>
      </div>
    `;
    return;
  }

  container.innerHTML = filtered.map(theme => {
    const userRating = userRatings[theme.id];
    const isLiked = !!userLikes[theme.id];
    const currentScore = userRating ? ((theme.baseRating * theme.baseRatingCount + userRating) / (theme.baseRatingCount + 1)).toFixed(2) : theme.baseRating.toFixed(2);
    const ratingCount = theme.baseRatingCount + (userRating ? 1 : 0);
    const likeCount = theme.baseLikes + (isLiked ? 1 : 0);
    const isCurrentActive = currentPreviewTheme.id === theme.id;

    return `
      <article class="theme-card ${isCurrentActive ? 'active-preview' : ''}" id="card-${theme.id}" style="--card-accent: ${theme.colors.primary};">
        <div class="theme-card-top">
          <div>
            <h3 class="theme-card-title">
              ${theme.name}
              <span class="badge-pill">${theme.version}</span>
            </h3>
            <span class="theme-card-author">Автор: ${theme.author} • ${theme.categoryLabel}</span>
          </div>
          <span class="badge-pill active">${theme.nameEn}</span>
        </div>

        <div class="rating-container">
          <div class="stars-row" title="Нажмите, чтобы оценить тему">
            ${[1, 2, 3, 4, 5].map(star => `
              <svg class="star-icon ${star <= Math.round(currentScore) ? 'filled' : ''}" 
                   viewBox="0 0 24 24" 
                   onclick="rateTheme('${theme.id}', ${star})">
                <path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/>
              </svg>
            `).join('')}
          </div>
          <span class="rating-score">${currentScore}</span>
          <span class="rating-count">(${ratingCount} оценок)</span>

          <button class="like-btn ${isLiked ? 'liked' : ''}" onclick="toggleThemeLike('${theme.id}')" title="Нравится">
            <svg viewBox="0 0 24 24"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
            <span>${likeCount}</span>
          </button>
        </div>

        <p class="theme-card-desc">${theme.description}</p>

        <div class="palette-row">
          <span class="palette-label">Палитра:</span>
          <div class="swatch-group">
            <div class="swatch-circle" style="background: ${theme.colors.primary};" title="Primary: ${theme.colors.primary}" onclick="copyHex('${theme.colors.primary}')"></div>
            <div class="swatch-circle" style="background: ${theme.colors.secondary};" title="Accent: ${theme.colors.secondary}" onclick="copyHex('${theme.colors.secondary}')"></div>
            <div class="swatch-circle" style="background: ${theme.colors.surface};" title="Surface: ${theme.colors.surface}" onclick="copyHex('${theme.colors.surface}')"></div>
            <div class="swatch-circle" style="background: ${theme.colors.background};" title="Background: ${theme.colors.background}" onclick="copyHex('${theme.colors.background}')"></div>
          </div>
        </div>

        <div class="theme-card-actions">
          <button class="btn btn-primary" onclick="installInOpenFy('${theme.id}', '${theme.downloadUrl}', '${theme.author}')" title="Установить сразу в плеер на телефоне">
            <svg viewBox="0 0 24 24"><path d="M19.35 10.04C18.67 6.59 15.64 4 12 4 9.11 4 6.6 5.64 5.35 8.04 2.34 8.36 0 10.91 0 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96zM17 13l-5 5-5-5h3V9h4v4h3z"/></svg>
            В OpenFy
          </button>

          <button class="btn btn-wifi" onclick="wifiSyncEngine.applyThemeViaWifi('${theme.id}')" title="Мгновенно применить на телефоне по Wi-Fi">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M12 4C7.31 4 3.07 5.9 0 8.98L12 21 24 8.98C20.93 5.9 16.69 4 12 4zm0 3.5c3.78 0 7.22 1.48 9.77 3.91L12 19.34 2.23 11.41C4.78 8.98 8.22 7.5 12 7.5z"/></svg>
            По Wi-Fi
          </button>

          <button class="btn btn-secondary" onclick="previewTheme('${theme.id}')">
            <svg viewBox="0 0 24 24"><path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"/></svg>
            Предпросмотр
          </button>

          <a href="${theme.downloadUrl}" download="${theme.id}.thm" class="btn btn-secondary" title="Скачать архив .thm">
            <svg viewBox="0 0 24 24"><path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z"/></svg>
            .thm
          </a>

          <button class="btn btn-secondary btn-icon-only" onclick="copyRawUrl('${theme.downloadUrl}')" title="Скопировать ссылку для OpenFy">
            <svg viewBox="0 0 24 24"><path d="M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z"/></svg>
          </button>

          <button class="btn btn-secondary btn-icon-only" onclick="openQrModal('${theme.name}', '${theme.id}', '${theme.downloadUrl}')" title="QR-код для быстрой установки">
            <svg viewBox="0 0 24 24"><path d="M3 3h8v8H3V3zm2 2v4h4V5H5zm8-2h8v8h-8V3zm2 2v4h4V5h-4zM3 13h8v8H3v-8zm2 2v4h4v-4H5zm13-2h3v2h-3v-2zm-5 0h2v3h-2v-3zm2 3h3v2h-3v-2zm3 2h3v3h-3v-3zm-3 2h2v2h-2v-2zm-2-2h2v2h-2v-2z"/></svg>
          </button>
        </div>
      </article>
    `;
  }).join('');
}

// Filter and Search Setup
function setupFilters() {
  const chips = document.querySelectorAll(".filter-chip");
  chips.forEach(chip => {
    chip.addEventListener("click", () => {
      chips.forEach(c => c.classList.remove("active"));
      chip.classList.add("active");
      activeCategory = chip.dataset.category || "all";
      renderThemeCards();
    });
  });
}

function setupSearch() {
  const searchInput = document.getElementById("search-input");
  if (!searchInput) return;
  searchInput.addEventListener("input", (e) => {
    searchQuery = e.target.value;
    renderThemeCards();
  });
}

function resetSearch() {
  const searchInput = document.getElementById("search-input");
  if (searchInput) searchInput.value = "";
  searchQuery = "";
  activeCategory = "all";
  const chips = document.querySelectorAll(".filter-chip");
  chips.forEach((c, idx) => c.classList.toggle("active", idx === 0));
  renderThemeCards();
}

// Rating & Like Handlers
window.rateTheme = function(id, score) {
  storage.saveRating(id, score);
  showToast(`Спасибо за вашу оценку ${score} ★!`);
  renderThemeCards();
};

window.toggleThemeLike = function(id) {
  const isLiked = storage.toggleLike(id);
  showToast(isLiked ? "Тема добавлена в понравившиеся!" : "Отметка «Нравится» удалена");
  renderThemeCards();
};

// ============================================================================
// Mockup Interaction & Theming Engine
// ============================================================================
function applyThemeToMockup(theme) {
  currentPreviewTheme = theme;
  const root = document.documentElement;
  const phoneScreen = document.querySelector(".phone-screen");

  root.style.setProperty("--theme-primary", theme.colors.primary);
  root.style.setProperty("--theme-on-primary", theme.colors.onPrimary || "#000000");
  root.style.setProperty("--theme-secondary", theme.colors.secondary);
  root.style.setProperty("--theme-background", theme.colors.background);
  root.style.setProperty("--theme-surface", theme.colors.surface);
  root.style.setProperty("--theme-surface-variant", theme.colors.surfaceVariant || theme.colors.surface);
  root.style.setProperty("--theme-on-surface", theme.colors.onSurface);
  root.style.setProperty("--theme-on-surface-variant", theme.colors.onSurfaceVariant);
  root.style.setProperty("--theme-accent", theme.colors.accent);
  root.style.setProperty("--theme-glow", theme.colors.glow || `${theme.colors.primary}66`);

  if (phoneScreen) {
    // Background style
    phoneScreen.classList.remove("bg-solid", "bg-gradient", "bg-radial", "bg-grid", "bg-aurora");
    const bgMap = {
      "SOLID": "bg-solid",
      "LINEAR_GRADIENT": "bg-gradient",
      "RADIAL_GLOW": "bg-radial",
      "CYBER_GRID": "bg-grid",
      "AURORA_MESH": "bg-aurora"
    };
    phoneScreen.classList.add(bgMap[theme.backgroundStyle] || "bg-solid");

    // Font style
    phoneScreen.classList.remove("font-outfit", "font-inter", "font-mono", "font-orbitron", "font-serif");
    const fontMap = {
      "OUTFIT": "font-outfit",
      "INTER": "font-inter",
      "MONO": "font-mono",
      "ORBITRON": "font-orbitron",
      "PLAYFAIR": "font-serif"
    };
    phoneScreen.classList.add(fontMap[theme.fontFamily] || "font-outfit");

    // Icon style
    phoneScreen.classList.remove("icons-rounded", "icons-sharp", "icons-line", "icons-glow");
    const iconMap = {
      "ROUNDED": "icons-rounded",
      "CYBER_SHARP": "icons-sharp",
      "MINIMAL_LINE": "icons-line",
      "NEON_GLOW": "icons-glow"
    };
    phoneScreen.classList.add(iconMap[theme.iconStyle] || "icons-rounded");
  }

  // Re-render mockup content to reflect artwork layout (vinyl vs card)
  renderMockupContent();

  // Highlight card
  document.querySelectorAll(".theme-card").forEach(c => c.classList.remove("active-preview"));
  const activeCard = document.getElementById(`card-${theme.id}`);
  if (activeCard) activeCard.classList.add("active-preview");
}

window.previewTheme = function(id) {
  const theme = THEMES_DATA.find(t => t.id === id);
  if (theme) {
    applyThemeToMockup(theme);
    showToast(`Предпросмотр: ${theme.name}`);
  }
};

// Deep Linking to OpenFy App
window.installInOpenFy = function(id, downloadUrl, creator) {
  const creatorParam = creator || (authEngine.currentUser ? authEngine.currentUser.username : "");
  const deepLink = `openfy://theme/install?id=${encodeURIComponent(id)}&url=${encodeURIComponent(downloadUrl)}&creator=${encodeURIComponent(creatorParam)}&apply=true`;
  
  const startTime = Date.now();
  window.location.href = deepLink;

  setTimeout(() => {
    if (Date.now() - startTime < 1500) {
      openQrModal("Установка темы в OpenFy", id, downloadUrl);
    }
  }, 1000);
};

// Copy Helpers
window.copyRawUrl = function(url) {
  navigator.clipboard.writeText(url).then(() => {
    showToast("Прямая ссылка на тему скопирована в буфер!");
  }).catch(() => {
    prompt("Скопируйте ссылку:", url);
  });
};

window.copyHex = function(hex) {
  navigator.clipboard.writeText(hex).then(() => {
    showToast(`Цвет ${hex} скопирован!`);
  });
};

// Mockup Controls
function setupMockupControls() {
  const tabBtns = document.querySelectorAll(".mockup-tab-btn");
  tabBtns.forEach(btn => {
    btn.addEventListener("click", () => {
      tabBtns.forEach(b => b.classList.remove("active"));
      btn.classList.add("active");
      currentMockupTab = btn.dataset.tab;
      renderMockupContent();
    });
  });

  bindPlayButton();
}

function bindPlayButton() {
  const playBtn = document.getElementById("mockup-play-btn");
  const vinylDisc = document.getElementById("vinyl-disc");
  if (playBtn) {
    playBtn.addEventListener("click", () => {
      isMockupPlaying = !isMockupPlaying;
      if (vinylDisc) vinylDisc.classList.toggle("playing", isMockupPlaying);
      playBtn.innerHTML = isMockupPlaying ? 
        `<svg viewBox="0 0 24 24"><path d="M6 19h4V5H6v14zm8-14v14h4V5h-4z"/></svg>` : 
        `<svg viewBox="0 0 24 24"><path d="M8 5v14l11-7z"/></svg>`;
    });
  }
}

function renderMockupContent() {
  const container = document.getElementById("mockup-screens-container");
  if (!container) return;

  const currentTheme = currentPreviewTheme || THEMES_DATA[0];
  const layout = currentTheme.playerLayout || "VINYL_DISC";

  if (currentMockupTab === "player") {
    const artworkHtml = layout === "ALBUM_CARD" ? `
      <div class="album-card-art">
        <svg viewBox="0 0 24 24"><path d="M12 3v10.55c-.59-.34-1.27-.55-2-.55-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4V7h4V3h-6z"/></svg>
      </div>
    ` : `
      <div class="vinyl-container">
        <div class="vinyl-disc ${isMockupPlaying ? 'playing' : ''}" id="vinyl-disc">
          <div class="vinyl-art">
            <svg viewBox="0 0 24 24"><path d="M12 3v10.55c-.59-.34-1.27-.55-2-.55-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4V7h4V3h-6z"/></svg>
          </div>
        </div>
      </div>
    `;

    container.innerHTML = `
      <div class="mockup-now-playing">
        <div class="mockup-top-nav">
          <svg viewBox="0 0 24 24"><path d="M7.41 8.59L12 13.17l4.59-4.58L18 10l-6 6-6-6 1.41-1.41z"/></svg>
          <span>СЕЙЧАС ИГРАЕТ</span>
          <svg viewBox="0 0 24 24"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 17.93c-3.95-.49-7-3.85-7-7.93 0-.62.08-1.21.21-1.79L9 15v1c0 1.1.9 2 2 2v1.93zm6.9-2.54c-.26-.81-1-1.39-1.9-1.39h-1v-3c0-.55-.45-1-1-1H8v-2h2c.55 0 1-.45 1-1V7h2c1.1 0 2-.9 2-2v-.41c2.93 1.19 5 4.06 5 7.41 0 2.08-.8 3.97-2.1 5.39z"/></svg>
        </div>

        ${artworkHtml}

        <div class="waveform-box" id="waveform-box">
          ${Array.from({ length: 24 }).map(() => `<div class="wave-bar"></div>`).join('')}
        </div>

        <div class="mockup-track-info">
          <div class="mockup-track-title">Cyber Samurai</div>
          <div class="mockup-track-artist">OpenFy Synth Lab • Synthwave 2026</div>
        </div>

        <div class="mockup-progress">
          <div class="progress-track">
            <div class="progress-fill"></div>
          </div>
          <div class="progress-times">
            <span>01:42</span>
            <span>03:36</span>
          </div>
        </div>

        <div class="mockup-controls">
          <button class="mockup-ctrl-btn"><svg viewBox="0 0 24 24"><path d="M10.59 9.17L5.41 4 4 5.41l5.17 5.17 1.42-1.41zM14.5 4l2.04 2.04L4 18.59 5.41 20 17.96 7.46 20 9.5V4h-5.5zm.33 9.41l-1.41 1.41 3.13 3.13L14.5 20H20v-5.5l-2.04 2.04-3.13-3.13z"/></svg></button>
          <button class="mockup-ctrl-btn"><svg viewBox="0 0 24 24"><path d="M6 6h2v12H6zm3.5 6l8.5 6V6z"/></svg></button>
          <button class="mockup-play-btn" id="mockup-play-btn">
            ${isMockupPlaying ? `<svg viewBox="0 0 24 24"><path d="M6 19h4V5H6v14zm8-14v14h4V5h-4z"/></svg>` : `<svg viewBox="0 0 24 24"><path d="M8 5v14l11-7z"/></svg>`}
          </button>
          <button class="mockup-ctrl-btn"><svg viewBox="0 0 24 24"><path d="M6 18l8.5-6L6 6v12zM16 6v12h2V6h-2z"/></svg></button>
          <button class="mockup-ctrl-btn"><svg viewBox="0 0 24 24"><path d="M7 7h10v3l4-4-4-4v3H5v6h2V7zm10 10H7v-3l-4 4 4 4v-3h12v-6h-2v4z"/></svg></button>
        </div>

        <div class="mockup-bottom-dock">
          <div class="dock-item"><svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor"><path d="M3 17v2h6v-2H3zM3 5v2h10V5H3zm10 16v-2h8v-2h-8v-2h-2v6h2zM7 9v2H3v2h4v2h2V9H7zm14 4v-2H11v2h10zm-6-4h2V7h4V5h-4V3h-2v6z"/></svg> Студия FX</div>
          <div class="dock-item">1.0x Норма</div>
          <div class="dock-item active">DJ Flow</div>
        </div>
      </div>
    `;
    bindPlayButton();
  } else if (currentMockupTab === "library") {
    container.innerHTML = `
      <div class="mockup-library">
        <div class="library-heading">Моя медиатека</div>
        <div class="playlist-preview-grid">
          <div class="playlist-card-mock"><div class="playlist-box-art"></div> Любимые треки</div>
          <div class="playlist-card-mock"><div class="playlist-box-art"></div> Ночной Дрифт</div>
          <div class="playlist-card-mock"><div class="playlist-box-art"></div> Киберчилл 2026</div>
          <div class="playlist-card-mock"><div class="playlist-box-art"></div> Рок и Энергия</div>
        </div>

        <div style="font-size: 0.8rem; font-weight: 700; margin: 12px 0 6px; color: var(--theme-primary);">ТОП ПРОСЛУШИВАНИЙ</div>
        ${[
          { n: "01", t: "Resonance", a: "HOME" },
          { n: "02", t: "Midnight City", a: "M83" },
          { n: "03", t: "Nightcall", a: "Kavinsky" },
          { n: "04", t: "Starboy", a: "The Weeknd" }
        ].map(track => `
          <div class="ranked-track-row">
            <span class="ranked-num">${track.n}</span>
            <div class="ranked-info">
              <div class="ranked-title">${track.t}</div>
              <div class="ranked-artist">${track.a}</div>
            </div>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="var(--theme-primary)"><path d="M8 5v14l11-7z"/></svg>
          </div>
        `).join('')}
      </div>
    `;
  } else if (currentMockupTab === "studio") {
    container.innerHTML = `
      <div class="mockup-studio">
        <div class="studio-module">
          <div class="studio-label"><span>КАРАОКЕ (APPLE SING)</span><span>85% ВЫРЕЗАНО</span></div>
          <div class="progress-track" style="margin: 6px 0;"><div class="progress-fill" style="width: 85%;"></div></div>
        </div>

        <div class="studio-module">
          <div class="studio-label"><span>СКОРОСТЬ И ТОНАЛЬНОСТЬ</span><span>NIGHTCORE</span></div>
          <div style="display: flex; gap: 4px; margin-top: 4px;">
            <span class="badge-pill active" style="font-size: 0.65rem;">1.25x</span>
            <span class="badge-pill active" style="font-size: 0.65rem;">+2 полутона</span>
          </div>
        </div>

        <div class="studio-module">
          <div class="studio-label"><span>5-ПОЛОСНЫЙ ЭКВАЛАЙЗЕР</span><span>BASS BOOST</span></div>
          <div class="eq-mock-bars">
            ${[
              { f: "60Hz", h: "75%" },
              { f: "230Hz", h: "60%" },
              { f: "910Hz", h: "45%" },
              { f: "3.6k", h: "50%" },
              { f: "14k", h: "65%" }
            ].map(col => `
              <div class="eq-col">
                <div class="eq-slider">
                  <div class="eq-thumb" style="bottom: ${col.h};"></div>
                </div>
                <span class="eq-freq">${col.f}</span>
              </div>
            `).join('')}
          </div>
        </div>
      </div>
    `;
  }
}

// Waveform simulation
function startWaveformAnimation() {
  setInterval(() => {
    if (!isMockupPlaying || currentMockupTab !== "player") return;
    const bars = document.querySelectorAll(".wave-bar");
    bars.forEach(bar => {
      const height = Math.floor(Math.random() * 26) + 6;
      bar.style.height = `${height}px`;
    });
  }, 120);
}

// ============================================================================
// Next-Gen Online Theme Studio (Multi-tab creator)
// ============================================================================
function setupThemeStudio() {
  // Setup Studio Tab Navigation
  const studioTabs = document.querySelectorAll(".studio-tab-nav");
  studioTabs.forEach(tab => {
    tab.addEventListener("click", () => {
      studioTabs.forEach(t => t.classList.remove("active"));
      tab.classList.add("active");
      const targetPanelId = tab.dataset.panel;
      document.querySelectorAll(".studio-tab-panel").forEach(p => p.classList.remove("active"));
      const targetPanel = document.getElementById(targetPanelId);
      if (targetPanel) targetPanel.classList.add("active");
    });
  });

  const pickers = {
    primary: document.getElementById("creator-primary"),
    secondary: document.getElementById("creator-secondary"),
    background: document.getElementById("creator-background"),
    surface: document.getElementById("creator-surface"),
    onSurface: document.getElementById("creator-onsurface")
  };

  if (!pickers.primary) return;

  function updateStudioTheme() {
    studioState.name = document.getElementById("creator-name")?.value || "Моя Студийная Тема";
    studioState.author = document.getElementById("creator-author")?.value || (authEngine.currentUser ? authEngine.currentUser.username : "OpenFy User");
    studioState.primary = pickers.primary.value;
    studioState.secondary = pickers.secondary.value;
    studioState.background = pickers.background.value;
    studioState.surface = pickers.surface.value;
    studioState.onSurface = pickers.onSurface.value;

    // Update Hex labels
    document.getElementById("hex-primary").textContent = pickers.primary.value.toUpperCase();
    document.getElementById("hex-secondary").textContent = pickers.secondary.value.toUpperCase();
    document.getElementById("hex-background").textContent = pickers.background.value.toUpperCase();
    document.getElementById("hex-surface").textContent = pickers.surface.value.toUpperCase();
    document.getElementById("hex-onsurface").textContent = pickers.onSurface.value.toUpperCase();

    const liveThemeObj = {
      id: "studio_custom_" + Date.now(),
      name: studioState.name,
      nameEn: "Custom Theme",
      author: studioState.author,
      version: studioState.version,
      category: "custom",
      categoryLabel: "Пользовательская",
      description: "Создана в онлайн-студии тем OpenFy Themes Hub",
      backgroundStyle: studioState.backgroundStyle,
      fontFamily: studioState.fontFamily,
      iconStyle: studioState.iconStyle,
      playerLayout: studioState.playerLayout,
      colors: {
        primary: studioState.primary,
        onPrimary: "#000000",
        secondary: studioState.secondary,
        background: studioState.background,
        surface: studioState.surface,
        surfaceVariant: studioState.surface,
        onSurface: studioState.onSurface,
        onSurfaceVariant: "#8C96AD",
        accent: studioState.secondary,
        card: studioState.surface,
        glow: `${studioState.primary}66`
      }
    };

    applyThemeToMockup(liveThemeObj);
  }

  Object.values(pickers).forEach(input => {
    if (input) input.addEventListener("input", updateStudioTheme);
  });

  document.getElementById("creator-name")?.addEventListener("input", updateStudioTheme);
  document.getElementById("creator-author")?.addEventListener("input", updateStudioTheme);

  // Background style selector
  window.selectBackgroundStyle = function(styleKey, el) {
    studioState.backgroundStyle = styleKey;
    document.querySelectorAll(".bg-pill").forEach(p => p.classList.remove("active"));
    if (el) el.classList.add("active");
    updateStudioTheme();
  };

  // Typography selector
  window.selectFontFamily = function(fontKey, el) {
    studioState.fontFamily = fontKey;
    document.querySelectorAll(".font-pill").forEach(p => p.classList.remove("active"));
    if (el) el.classList.add("active");
    updateStudioTheme();
  };

  // Icon style selector
  window.selectIconStyle = function(iconKey, el) {
    studioState.iconStyle = iconKey;
    document.querySelectorAll(".icon-pill").forEach(p => p.classList.remove("active"));
    if (el) el.classList.add("active");
    updateStudioTheme();
  };

  // Player layout selector
  window.selectPlayerLayout = function(layoutKey, el) {
    studioState.playerLayout = layoutKey;
    document.querySelectorAll(".layout-pill").forEach(p => p.classList.remove("active"));
    if (el) el.classList.add("active");
    updateStudioTheme();
  };

  // Preset Buttons
  window.applyStudioPreset = function(primary, secondary, bg, surf, onSurf, bgStyle = "SOLID", font = "OUTFIT") {
    pickers.primary.value = primary;
    pickers.secondary.value = secondary;
    pickers.background.value = bg;
    pickers.surface.value = surf;
    pickers.onSurface.value = onSurf;
    studioState.backgroundStyle = bgStyle;
    studioState.fontFamily = font;
    updateStudioTheme();
    showToast("Пресет палитры применён в студии!");
  };

  // Export JSON
  window.exportCustomThemeJson = function() {
    const themeJson = generateThemeJsonContent();
    const blob = new Blob([JSON.stringify(themeJson, null, 2)], { type: "application/json" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `${themeJson.id}.json`;
    a.click();
    URL.revokeObjectURL(url);
    showToast("Файл темы успешно сгенерирован и скачан!");
  };

  // Copy JSON
  window.copyCustomThemeJson = function() {
    const themeJson = generateThemeJsonContent();
    navigator.clipboard.writeText(JSON.stringify(themeJson, null, 2)).then(() => {
      showToast("Полная конфигурация темы скопирована в буфер!");
    });
  };

  // Open QR modal for current Studio theme
  window.openStudioQrModal = function() {
    const themeJson = generateThemeJsonContent();
    const id = themeJson.id;
    const author = themeJson.author;
    const jsonStr = JSON.stringify(themeJson);
    const deepLink = `openfy://theme/install?id=${encodeURIComponent(id)}&creator=${encodeURIComponent(author)}&data=${encodeURIComponent(jsonStr)}&apply=true`;
    openQrModal(`Тема «${themeJson.name}» (100% Офлайн)`, id, deepLink);
  };

  // Install in OpenFy directly from Studio
  window.installStudioThemeInOpenFy = function() {
    const themeJson = generateThemeJsonContent();
    const id = themeJson.id;
    const author = themeJson.author;
    const deepLink = `openfy://theme/install?id=${encodeURIComponent(id)}&creator=${encodeURIComponent(author)}&apply=true`;
    
    const startTime = Date.now();
    window.location.href = deepLink;
    showToast(`Переход в OpenFy для установки темы «${themeJson.name}»...`);

    setTimeout(() => {
      if (Date.now() - startTime < 1500) {
        openQrModal(`Установка «${themeJson.name}» в OpenFy`, id, deepLink);
      }
    }, 1000);
  };
}

function generateThemeJsonContent() {
  const name = studioState.name || "Custom Theme";
  const author = studioState.author || (authEngine.currentUser ? authEngine.currentUser.username : "OpenFy User");
  const id = name.toLowerCase().replace(/[^a-z0-9]/g, "_") || "custom_theme";

  return {
    id: id,
    name: name,
    author: author,
    version: studioState.version || "1.0.0",
    isDark: true,
    backgroundStyle: studioState.backgroundStyle,
    fontFamily: studioState.fontFamily,
    iconStyle: studioState.iconStyle,
    playerLayout: studioState.playerLayout,
    creatorProfile: authEngine.currentUser ? {
      username: authEngine.currentUser.username,
      provider: authEngine.currentUser.provider,
      avatarUrl: authEngine.currentUser.avatarUrl
    } : null,
    colors: {
      primary: studioState.primary,
      onPrimary: "#000000",
      secondary: studioState.secondary,
      background: studioState.background,
      surface: studioState.surface,
      surfaceVariant: studioState.surface,
      onSurface: studioState.onSurface,
      onSurfaceVariant: "#8C96AD",
      accent: studioState.secondary,
      cardColor: studioState.surface
    }
  };
}

// ============================================================================
// Auth Modal Handlers
// ============================================================================
window.openAuthModal = function() {
  const modal = document.getElementById("auth-modal");
  if (modal) modal.classList.add("open");
};

window.closeAuthModal = function() {
  const modal = document.getElementById("auth-modal");
  if (modal) modal.classList.remove("open");
};

window.switchAuthTab = function(tabName, el) {
  document.querySelectorAll(".auth-tab-btn").forEach(b => b.classList.remove("active"));
  if (el) el.classList.add("active");

  document.querySelectorAll(".auth-tab-panel").forEach(p => p.classList.remove("active"));
  const panel = document.getElementById(`auth-panel-${tabName}`);
  if (panel) panel.classList.add("active");
};

// QR Code Modal
let currentQrData = {
  title: "",
  id: "",
  url: "",
  deepLink: "",
  mode: "deeplink"
};

window.openQrModal = function(title, id, url) {
  const modal = document.getElementById("qr-modal");
  const modalTitle = document.getElementById("qr-modal-title");
  const qrContainer = document.getElementById("qr-container");
  const directLinkBtn = document.getElementById("qr-direct-link");
  if (!modal || !qrContainer) return;

  modalTitle.textContent = title;
  const creator = authEngine.currentUser ? authEngine.currentUser.username : "";
  const deepLink = `openfy://theme/install?id=${encodeURIComponent(id)}&url=${encodeURIComponent(url)}&creator=${encodeURIComponent(creator)}&apply=true`;

  currentQrData = {
    title: title,
    id: id,
    url: url,
    deepLink: deepLink,
    mode: "deeplink"
  };

  if (directLinkBtn) directLinkBtn.href = deepLink;

  const tabDeeplink = document.getElementById("qr-tab-deeplink");
  const tabUrl = document.getElementById("qr-tab-url");
  const scanHint = document.getElementById("qr-scan-hint");
  if (tabDeeplink && tabUrl) {
    tabDeeplink.classList.add("active");
    tabUrl.classList.remove("active");
    if (scanHint) scanHint.textContent = "Диплинк: мгновенная установка в плеер OpenFy через камеру или сканер.";
  }

  updateQrDisplay();
  modal.classList.add("open");
};

window.switchQrMode = function(mode) {
  currentQrData.mode = mode;
  const tabDeeplink = document.getElementById("qr-tab-deeplink");
  const tabUrl = document.getElementById("qr-tab-url");
  const scanHint = document.getElementById("qr-scan-hint");
  if (tabDeeplink && tabUrl) {
    if (mode === "deeplink") {
      tabDeeplink.classList.add("active");
      tabUrl.classList.remove("active");
      if (scanHint) scanHint.textContent = "Диплинк: мгновенная установка в плеер OpenFy через камеру или сканер.";
    } else {
      tabUrl.classList.add("active");
      tabDeeplink.classList.remove("active");
      if (scanHint) scanHint.textContent = "Прямая ссылка: для загрузки .thm архива или открытия в браузере.";
    }
  }
  updateQrDisplay();
};

function updateQrDisplay() {
  const qrContainer = document.getElementById("qr-container");
  if (!qrContainer) return;

  const targetText = currentQrData.mode === "deeplink" ? currentQrData.deepLink : currentQrData.url;
  qrContainer.innerHTML = generateSvgQrCode(targetText);
}

window.closeQrModal = function() {
  const modal = document.getElementById("qr-modal");
  if (modal) modal.classList.remove("open");
};

// Standard ISO/IEC 18004 QR Code Generator using qrcode-generator
function generateSvgQrCode(text) {
  try {
    if (typeof qrcode !== "undefined") {
      // typeNumber: 0 (auto), errorCorrectionLevel: 'M'
      const qr = qrcode(0, 'M');
      qr.addData(text);
      qr.make();
      return qr.createSvgTag({ cellSize: 4, margin: 2, scalable: false });
    }
  } catch (err) {
    console.error("QR Code Generation failed:", err);
  }
  return '<p style="color:#ef4444;font-size:0.85rem;padding:2rem;">Ошибка генерации QR-кода</p>';
}

// Toast Notifications
function showToast(message) {
  let container = document.getElementById("toast-container");
  if (!container) {
    container = document.createElement("div");
    container.id = "toast-container";
    container.className = "toast-container";
    document.body.appendChild(container);
  }

  const toast = document.createElement("div");
  toast.className = "toast";
  toast.innerHTML = `
    <svg width="18" height="18" viewBox="0 0 24 24" fill="var(--theme-primary)"><path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/></svg>
    <span>${message}</span>
  `;
  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = "0";
    toast.style.transform = "translateY(20px)";
    toast.style.transition = "all 0.3s ease";
    setTimeout(() => toast.remove(), 300);
  }, 2800);
}

// ============================================================================
// Security 2FA & Wi-Fi Modal Helpers
// ============================================================================
window.open2FaModal = function() {
  const modal = document.getElementById("security-2fa-modal");
  if (modal) modal.classList.add("open");
};

window.close2FaModal = function() {
  const modal = document.getElementById("security-2fa-modal");
  if (modal) modal.classList.remove("open");
};

window.openWifiModal = function() {
  const modal = document.getElementById("wifi-connect-modal");
  const input = document.getElementById("wifi-ip-input");
  if (input && wifiSyncEngine.ip) input.value = wifiSyncEngine.ip;
  if (modal) modal.classList.add("open");
  if (wifiSyncEngine.ip) wifiSyncEngine.pingPlayer(wifiSyncEngine.ip, false);
};

window.closeWifiModal = function() {
  const modal = document.getElementById("wifi-connect-modal");
  if (modal) modal.classList.remove("open");
};

window.toggleMobileNav = function() {
  const drawer = document.getElementById("mobile-nav-drawer");
  if (drawer) drawer.classList.toggle("open");
};
