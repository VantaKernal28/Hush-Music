$raw = Get-Content "C:\Users\mirag\.gemini\antigravity\scratch\hush-music\rendered_hushmusic.html" -Raw

# 1. Clean head
$html = $raw
$html = [regex]::Replace($html, '<link rel="preload" href="/_next/[^>]+>', '')
$html = [regex]::Replace($html, '<link rel="preload" as="script"[^>]+>', '')
$html = [regex]::Replace($html, '<link rel="stylesheet" href="/_next/static/chunks/3pvva-rvoc5p4.css"[^>]+>', '<link rel="stylesheet" href="styles.css">')
$html = [regex]::Replace($html, '<link rel="preload" href="/_next/static/chunks/2m2t_sp8iavzd.css"[^>]+>', '')
$html = [regex]::Replace($html, '<next-route-announcer[^>]*></next-route-announcer>', '')

# Replace Google Fonts in head
$fontsLink = @'
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&family=JetBrains+Mono:wght@400;500;600&family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
'@

$html = $html.Replace('<meta charset="utf-8">', "<meta charset=`"utf-8`">`n$fontsLink")

# Remove data-paused=""
$html = $html.Replace('data-paused=""', '')

# Replace local paths
$html = $html.Replace('/FullLogoTransparent.png', 'public/FullLogoTransparent.png')
$html = $html.Replace('/logo-mark.svg', 'public/logo-mark.svg')
$html = $html.Replace('/tv/now-playing.webp', 'public/tv/now-playing.webp')
$html = $html.Replace('/tv/lyrics.webp', 'public/tv/lyrics.webp')
$html = $html.Replace('/tv/lyrics-pride.webp', 'public/tv/lyrics-pride.webp')
$html = $html.Replace('/tv/up-next.webp', 'public/tv/up-next.webp')
$html = $html.Replace('/albums/coldplay-parachutes.png', 'public/albums/coldplay-parachutes.png')
$html = [regex]::Replace($html, '/favicon\.ico\?favicon\.[^"]+', 'public/favicon.ico')
$html = [regex]::Replace($html, '/icon\.svg\?icon\.[^"]+', 'public/logo-mark.svg')
$html = [regex]::Replace($html, '/apple-icon\.png\?apple-icon\.[^"]+', 'public/apple-icon.png')

# Fix URLs
$html = $html.Replace('http://localhost:3100', 'https://hushmusic.app')
$html = $html.Replace('https://hushmusic.kushagrasingh.in', 'https://hushmusic.app')
$html = $html.Replace('https://hushmusic-8bw.pages.dev/', 'https://hushmusic.app')
$html = $html.Replace('https://github.com/kushagrasinghx/hushmusic', 'https://github.com/hushmusic/hush-music')

# Replace Brand text
$html = $html.Replace("hushmusic's", "Hush Music's")
$html = $html.Replace("hushmusic", "Hush Music")
$html = $html.Replace("hushmusic", "hush-music")

# Add interactivity script before </body>
$jsScript = @'
<script>
document.addEventListener("DOMContentLoaded", function () {
  // 1. TV Screen switcher tabs
  const tablist = document.querySelector('[role="tablist"][aria-label="TV screenshots"]');
  if (tablist) {
    const tabs = tablist.querySelectorAll('[role="tab"]');
    const tvImg = document.querySelector('img[alt*="Hush Music on TV"]');
    const tvScreens = [
      { src: "public/tv/now-playing.webp", alt: "Hush Music on TV: Now Playing" },
      { src: "public/tv/lyrics.webp", alt: "Hush Music on TV: Synced Lyrics" },
      { src: "public/tv/lyrics-pride.webp", alt: "Hush Music on TV: Synced Lyrics (Pride)" },
      { src: "public/tv/up-next.webp", alt: "Hush Music on TV: Up Next" }
    ];

    tabs.forEach((tab, index) => {
      tab.addEventListener("click", () => {
        tabs.forEach((t) => {
          t.setAttribute("aria-selected", "false");
          t.className = "h-1.5 rounded-full transition-all duration-300 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-white/70 w-1.5 bg-white/25 hover:bg-white/40";
        });
        tab.setAttribute("aria-selected", "true");
        tab.className = "h-1.5 rounded-full transition-all duration-300 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-white/70 w-6 bg-white";

        if (tvImg && tvScreens[index]) {
          tvImg.style.opacity = "0.2";
          tvImg.style.transition = "opacity 0.25s ease-in-out";
          setTimeout(() => {
            tvImg.src = tvScreens[index].src;
            tvImg.alt = tvScreens[index].alt;
            tvImg.style.opacity = "1";
          }, 150);
        }
      });
    });
  }

  // 2. Replay cards carousel
  const replayUl = document.querySelector('ul.snap-x');
  const prevReplayBtn = document.querySelector('button[aria-label="Previous Replay card"]');
  const nextReplayBtn = document.querySelector('button[aria-label="Next Replay card"]');

  function updateReplayBtns() {
    if (!replayUl || !prevReplayBtn || !nextReplayBtn) return;
    const atStart = replayUl.scrollLeft <= 10;
    const atEnd = replayUl.scrollLeft + replayUl.clientWidth >= replayUl.scrollWidth - 10;

    prevReplayBtn.disabled = atStart;
    prevReplayBtn.className = atStart
      ? "grid size-7 place-items-center text-white/70 transition-colors duration-300 cursor-default opacity-30"
      : "grid size-7 place-items-center text-white/70 transition-colors duration-300 hover:text-white";

    nextReplayBtn.disabled = atEnd;
    nextReplayBtn.className = atEnd
      ? "grid size-7 place-items-center text-white/70 transition-colors duration-300 cursor-default opacity-30"
      : "grid size-7 place-items-center text-white/70 transition-colors duration-300 hover:text-white";
  }

  if (replayUl && prevReplayBtn && nextReplayBtn) {
    prevReplayBtn.addEventListener("click", () => {
      replayUl.scrollBy({ left: -320, behavior: "smooth" });
    });
    nextReplayBtn.addEventListener("click", () => {
      replayUl.scrollBy({ left: 320, behavior: "smooth" });
    });
    replayUl.addEventListener("scroll", updateReplayBtns, { passive: true });
    updateReplayBtns();
  }

  // 3. Music Carousels (Best New Songs, New This Week, Recent Releases)
  const carouselTitles = ["Best New Songs", "New This Week", "Recent Releases"];
  carouselTitles.forEach((title) => {
    const heading = Array.from(document.querySelectorAll("h2, h3")).find((el) => el.textContent.trim() === title);
    if (!heading) return;
    const container = heading.closest("section, div.space-y-4, div.relative") || heading.parentElement;
    const prevBtn = document.querySelector(`button[aria-label="Scroll ${title} left"]`);
    const nextBtn = document.querySelector(`button[aria-label="Scroll ${title} right"]`);
    
    const scrollList = container ? container.querySelector("ul, div.overflow-x-auto") : null;
    if (!scrollList) return;

    function updateBtns() {
      if (prevBtn) {
        const atStart = scrollList.scrollLeft <= 10;
        prevBtn.disabled = atStart;
        prevBtn.className = atStart
          ? "grid size-7 place-items-center text-white/70 transition-colors duration-300 cursor-default opacity-30"
          : "grid size-7 place-items-center text-white/70 transition-colors duration-300 hover:text-white";
      }
      if (nextBtn) {
        const atEnd = scrollList.scrollLeft + scrollList.clientWidth >= scrollList.scrollWidth - 10;
        nextBtn.disabled = atEnd;
        nextBtn.className = atEnd
          ? "grid size-7 place-items-center text-white/70 transition-colors duration-300 cursor-default opacity-30"
          : "grid size-7 place-items-center text-white/70 transition-colors duration-300 hover:text-white";
      }
    }

    if (prevBtn) {
      prevBtn.addEventListener("click", () => {
        scrollList.scrollBy({ left: -600, behavior: "smooth" });
      });
    }
    if (nextBtn) {
      nextBtn.addEventListener("click", () => {
        scrollList.scrollBy({ left: 600, behavior: "smooth" });
      });
    }
    scrollList.addEventListener("scroll", updateBtns, { passive: true });
    updateBtns();
  });
});
</script>
'@

$html = $html.Replace('</body>', "$jsScript`n</body>")

[System.IO.File]::WriteAllText("C:\Users\mirag\.gemini\antigravity\scratch\hush-music\index.html", $html, [System.Text.Encoding]::UTF8)
Write-Host "Generated index.html successfully!"
