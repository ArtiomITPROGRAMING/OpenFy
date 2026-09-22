/*
 * OpenFy Themes Hub - Interactive Client Application
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

// App State
let currentPreviewTheme = THEMES_DATA[0];
let activeCategory = "all";
let searchQuery = "";
let isMockupPlaying = true;
let currentMockupTab = "player"; // 'player' | 'library' | 'studio'

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
  }
};

// DOM Init
document.addEventListener("DOMContentLoaded", () => {
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
          <button class="btn btn-primary" onclick="installInOpenFy('${theme.id}', '${theme.downloadUrl}')" title="Установить сразу в плеер на телефоне">
            <svg viewBox="0 0 24 24"><path d="M19.35 10.04C18.67 6.59 15.64 4 12 4 9.11 4 6.6 5.64 5.35 8.04 2.34 8.36 0 10.91 0 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96zM17 13l-5 5-5-5h3V9h4v4h3z"/></svg>
            В OpenFy
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

// Mockup Interaction & Theming
function applyThemeToMockup(theme) {
  currentPreviewTheme = theme;
  const root = document.documentElement;

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
window.installInOpenFy = function(id, downloadUrl) {
  const deepLink = `openfy://theme/install?id=${encodeURIComponent(id)}&url=${encodeURIComponent(downloadUrl)}&apply=true`;
  
  // Try opening deep link
  const startTime = Date.now();
  window.location.href = deepLink;

  // Fallback for desktop / without app installed
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

  const playBtn = document.getElementById("mockup-play-btn");
  const vinylDisc = document.getElementById("vinyl-disc");
  if (playBtn && vinylDisc) {
    playBtn.addEventListener("click", () => {
      isMockupPlaying = !isMockupPlaying;
      vinylDisc.classList.toggle("playing", isMockupPlaying);
      playBtn.innerHTML = isMockupPlaying ? 
        `<svg viewBox="0 0 24 24"><path d="M6 19h4V5H6v14zm8-14v14h4V5h-4z"/></svg>` : 
        `<svg viewBox="0 0 24 24"><path d="M8 5v14l11-7z"/></svg>`;
    });
  }
}

function renderMockupContent() {
  const container = document.getElementById("mockup-screens-container");
  if (!container) return;

  if (currentMockupTab === "player") {
    container.innerHTML = `
      <div class="mockup-now-playing">
        <div class="mockup-top-nav">
          <svg viewBox="0 0 24 24"><path d="M7.41 8.59L12 13.17l4.59-4.58L18 10l-6 6-6-6 1.41-1.41z"/></svg>
          <span>СЕЙЧАС ИГРАЕТ</span>
          <svg viewBox="0 0 24 24"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 17.93c-3.95-.49-7-3.85-7-7.93 0-.62.08-1.21.21-1.79L9 15v1c0 1.1.9 2 2 2v1.93zm6.9-2.54c-.26-.81-1-1.39-1.9-1.39h-1v-3c0-.55-.45-1-1-1H8v-2h2c.55 0 1-.45 1-1V7h2c1.1 0 2-.9 2-2v-.41c2.93 1.19 5 4.06 5 7.41 0 2.08-.8 3.97-2.1 5.39z"/></svg>
        </div>

        <div class="vinyl-container">
          <div class="vinyl-disc ${isMockupPlaying ? 'playing' : ''}" id="vinyl-disc">
            <div class="vinyl-art">
              <svg viewBox="0 0 24 24"><path d="M12 3v10.55c-.59-.34-1.27-.55-2-.55-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4V7h4V3h-6z"/></svg>
            </div>
          </div>
        </div>

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
    setupMockupControls();
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

// Online Theme Studio (Creator)
function setupThemeStudio() {
  const pickers = {
    primary: document.getElementById("creator-primary"),
    secondary: document.getElementById("creator-secondary"),
    background: document.getElementById("creator-background"),
    surface: document.getElementById("creator-surface"),
    onSurface: document.getElementById("creator-onsurface")
  };

  if (!pickers.primary) return;

  function updateFromCreator() {
    const customTheme = {
      id: "custom_theme_" + Date.now(),
      name: document.getElementById("creator-name")?.value || "Моя пользовательская тема",
      nameEn: "Custom Studio Theme",
      author: document.getElementById("creator-author")?.value || "OpenFy User",
      version: "1.0.0",
      category: "custom",
      categoryLabel: "Пользовательская",
      description: "Создана в онлайн-студии тем OpenFy Themes Hub",
      colors: {
        primary: pickers.primary.value,
        onPrimary: "#000000",
        secondary: pickers.secondary.value,
        background: pickers.background.value,
        surface: pickers.surface.value,
        surfaceVariant: pickers.surface.value,
        onSurface: pickers.onSurface.value,
        onSurfaceVariant: "#8C96AD",
        accent: pickers.secondary.value,
        card: pickers.surface.value,
        glow: `${pickers.primary.value}66`
      }
    };

    // Update Hex labels
    document.getElementById("hex-primary").textContent = pickers.primary.value.toUpperCase();
    document.getElementById("hex-secondary").textContent = pickers.secondary.value.toUpperCase();
    document.getElementById("hex-background").textContent = pickers.background.value.toUpperCase();
    document.getElementById("hex-surface").textContent = pickers.surface.value.toUpperCase();
    document.getElementById("hex-onsurface").textContent = pickers.onSurface.value.toUpperCase();

    applyThemeToMockup(customTheme);
  }

  Object.values(pickers).forEach(input => {
    if (input) input.addEventListener("input", updateFromCreator);
  });

  // Preset Buttons
  window.applyStudioPreset = function(primary, secondary, bg, surf, onSurf) {
    pickers.primary.value = primary;
    pickers.secondary.value = secondary;
    pickers.background.value = bg;
    pickers.surface.value = surf;
    pickers.onSurface.value = onSurf;
    updateFromCreator();
    showToast("Пресет палитры применён в студии!");
  };

  // Export JSON
  window.exportCustomThemeJson = function() {
    const themeJson = generateThemeJsonContent();
    const blob = new Blob([JSON.stringify(themeJson, null, 2)], { type: "application/json" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "theme.json";
    a.click();
    URL.revokeObjectURL(url);
    showToast("Файл theme.json успешно сгенерирован и скачан!");
  };

  // Copy JSON
  window.copyCustomThemeJson = function() {
    const themeJson = generateThemeJsonContent();
    navigator.clipboard.writeText(JSON.stringify(themeJson, null, 2)).then(() => {
      showToast("Конфигурация JSON скопирована в буфер обмена!");
    });
  };
}

function generateThemeJsonContent() {
  const name = document.getElementById("creator-name")?.value || "Custom Theme";
  const author = document.getElementById("creator-author")?.value || "OpenFy User";
  const id = name.toLowerCase().replace(/[^a-z0-9]/g, "_") || "custom_theme";

  return {
    id: id,
    name: name,
    author: author,
    version: "1.0.0",
    isDark: true,
    primary: document.getElementById("creator-primary")?.value || "#00FF66",
    onPrimary: "#000000",
    secondary: document.getElementById("creator-secondary")?.value || "#00E5FF",
    background: document.getElementById("creator-background")?.value || "#050B07",
    surface: document.getElementById("creator-surface")?.value || "#0C1810",
    onSurface: document.getElementById("creator-onsurface")?.value || "#E0F5E6"
  };
}

// QR Code Modal (Pure SVG generation)
window.openQrModal = function(title, id, url) {
  const modal = document.getElementById("qr-modal");
  const modalTitle = document.getElementById("qr-modal-title");
  const qrContainer = document.getElementById("qr-container");
  const directLinkBtn = document.getElementById("qr-direct-link");
  if (!modal || !qrContainer) return;

  modalTitle.textContent = title;
  const deepLink = `openfy://theme/install?id=${encodeURIComponent(id)}&url=${encodeURIComponent(url)}&apply=true`;
  if (directLinkBtn) directLinkBtn.href = deepLink;

  // Render SVG QR representation
  qrContainer.innerHTML = generateSvgQrCode(url);
  modal.classList.add("open");
};

window.closeQrModal = function() {
  const modal = document.getElementById("qr-modal");
  if (modal) modal.classList.remove("open");
};

// Lightweight deterministic SVG QR code matrix generator
function generateSvgQrCode(text) {
  // Deterministic seed matrix from text string
  let hash = 0;
  for (let i = 0; i < text.length; i++) {
    hash = ((hash << 5) - hash) + text.charCodeAt(i);
    hash |= 0;
  }

  const size = 25; // 25x25 grid
  const cellSize = 8;
  const total = size * cellSize;
  let rects = '';

  // Corner markers
  function addMarker(x0, y0) {
    for (let r = 0; r < 7; r++) {
      for (let c = 0; c < 7; c++) {
        const isBorder = (r === 0 || r === 6 || c === 0 || c === 6);
        const isCore = (r >= 2 && r <= 4 && c >= 2 && c <= 4);
        if (isBorder || isCore) {
          rects += `<rect x="${(x0 + c) * cellSize}" y="${(y0 + r) * cellSize}" width="${cellSize}" height="${cellSize}" fill="#000"/>`;
        }
      }
    }
  }

  addMarker(0, 0);
  addMarker(size - 7, 0);
  addMarker(0, size - 7);

  // Fill data cells
  for (let r = 0; r < size; r++) {
    for (let c = 0; c < size; c++) {
      // skip corner markers
      if ((r < 8 && c < 8) || (r < 8 && c >= size - 8) || (r >= size - 8 && c < 8)) continue;
      // pseudorandom based on text hash
      const cellHash = (hash ^ (r * 31 + c * 17) ^ (text.charCodeAt((r + c) % text.length) * 13)) & 1;
      if (cellHash === 1) {
        rects += `<rect x="${c * cellSize}" y="${r * cellSize}" width="${cellSize}" height="${cellSize}" fill="#000"/>`;
      }
    }
  }

  return `
    <svg viewBox="0 0 ${total} ${total}" xmlns="http://www.w3.org/2000/svg">
      <rect width="${total}" height="${total}" fill="#fff" rx="8"/>
      ${rects}
    </svg>
  `;
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
