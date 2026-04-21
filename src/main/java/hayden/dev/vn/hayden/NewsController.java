package hayden.dev.vn.hayden;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NewsController {
    private final Map<Long, NewsArticle> store = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(0);

    public NewsController() {
        seedData();
    }

    @GetMapping(value = "/news-app", produces = MediaType.TEXT_HTML_VALUE)
    public String newsApp() {
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>News CRUD Demo</title>
                    <style>
                        * { box-sizing: border-box; }
                        body {
                            margin: 0;
                            font-family: Arial, sans-serif;
                            background: linear-gradient(180deg, #0f172a, #020617);
                            color: #e2e8f0;
                        }
                        .container { width: min(1100px, 94vw); margin: 26px auto 40px; }
                        h1 {
                            text-align: center;
                            margin-bottom: 18px;
                            font-size: 2rem;
                            letter-spacing: 0.3px;
                        }
                        .panel {
                            background: rgba(15, 23, 42, 0.8);
                            border: 1px solid rgba(148, 163, 184, 0.3);
                            border-radius: 16px;
                            padding: 16px;
                            margin-bottom: 18px;
                        }
                        .form-grid {
                            display: grid;
                            grid-template-columns: repeat(2, minmax(0, 1fr));
                            gap: 10px;
                        }
                        input, textarea, button, select {
                            width: 100%;
                            border: 1px solid rgba(148, 163, 184, 0.35);
                            background: #0b1220;
                            color: #e2e8f0;
                            border-radius: 10px;
                            padding: 11px;
                            font-size: 0.95rem;
                        }
                        textarea { min-height: 90px; resize: vertical; }
                        .full { grid-column: 1 / -1; }
                        .actions { display: flex; gap: 10px; margin-top: 10px; }
                        button {
                            cursor: pointer;
                            font-weight: 700;
                            background: linear-gradient(135deg, #22d3ee, #3b82f6);
                            color: #0b1020;
                        }
                        .secondary {
                            background: linear-gradient(135deg, #f59e0b, #fb7185);
                        }
                        .cards {
                            display: grid;
                            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
                            gap: 14px;
                        }
                        .card {
                            overflow: hidden;
                            border-radius: 14px;
                            border: 1px solid rgba(148, 163, 184, 0.28);
                            background: rgba(15, 23, 42, 0.85);
                        }
                        .card img {
                            width: 100%;
                            height: 150px;
                            object-fit: cover;
                            display: block;
                        }
                        .card-body { padding: 12px; }
                        .badge {
                            display: inline-block;
                            background: #1d4ed8;
                            border-radius: 999px;
                            padding: 4px 10px;
                            font-size: 0.8rem;
                            margin-bottom: 8px;
                        }
                        .card h3 { margin: 6px 0 8px; font-size: 1.05rem; }
                        .card p {
                            margin: 0 0 10px;
                            color: #cbd5e1;
                            line-height: 1.45;
                            font-size: 0.92rem;
                        }
                        .row { display: flex; gap: 8px; }
                        .danger { background: linear-gradient(135deg, #fb7185, #ef4444); }
                        .ok { margin: 10px 0; color: #22c55e; font-weight: 700; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h1>News CRUD Demo - Bao/Tin tuc/Cong nghe</h1>
                        <section class="panel">
                            <div id="message" class="ok"></div>
                            <input type="hidden" id="articleId">
                            <div class="form-grid">
                                <input id="title" placeholder="Tieu de bai viet">
                                <select id="category">
                                    <option>Bao va tin tuc</option>
                                    <option>Cong nghe</option>
                                    <option>Cach tan gai</option>
                                    <option>Cach da bong</option>
                                    <option>Cach choi cau long</option>
                                </select>
                                <input id="imageUrl" placeholder="URL hinh anh">
                                <input id="shortNote" placeholder="Tom tat ngan">
                                <textarea id="content" class="full" placeholder="Noi dung chi tiet"></textarea>
                            </div>
                            <div class="actions">
                                <button onclick="saveArticle()">Luu bai viet</button>
                                <button class="secondary" onclick="resetForm()">Tao moi</button>
                            </div>
                        </section>
                        <section class="cards" id="cards"></section>
                    </div>
                    <script>
                        const api = "/api/articles";
                        const state = { items: [] };

                        async function loadArticles() {
                            const res = await fetch(api);
                            state.items = await res.json();
                            renderCards();
                        }

                        function renderCards() {
                            const cards = document.getElementById("cards");
                            cards.innerHTML = state.items.map((item) => `
                                <article class="card">
                                    <img src="${item.imageUrl}" alt="${item.title}">
                                    <div class="card-body">
                                        <span class="badge">${item.category}</span>
                                        <h3>${item.title}</h3>
                                        <p>${item.content}</p>
                                        <div class="row">
                                            <button onclick="editArticle(${item.id})">Sua</button>
                                            <button class="danger" onclick="deleteArticle(${item.id})">Xoa</button>
                                        </div>
                                    </div>
                                </article>
                            `).join("");
                        }

                        function editArticle(id) {
                            const item = state.items.find((x) => x.id === id);
                            if (!item) return;
                            document.getElementById("articleId").value = item.id;
                            document.getElementById("title").value = item.title;
                            document.getElementById("category").value = item.category;
                            document.getElementById("imageUrl").value = item.imageUrl;
                            document.getElementById("shortNote").value = "";
                            document.getElementById("content").value = item.content;
                            showMessage("Dang chinh sua bai #" + id);
                        }

                        function resetForm() {
                            document.getElementById("articleId").value = "";
                            document.getElementById("title").value = "";
                            document.getElementById("category").value = "Bao va tin tuc";
                            document.getElementById("imageUrl").value = "";
                            document.getElementById("shortNote").value = "";
                            document.getElementById("content").value = "";
                            showMessage("Da reset form.");
                        }

                        async function saveArticle() {
                            const id = document.getElementById("articleId").value;
                            const shortNote = document.getElementById("shortNote").value.trim();
                            const payload = {
                                title: document.getElementById("title").value.trim(),
                                category: document.getElementById("category").value,
                                imageUrl: document.getElementById("imageUrl").value.trim(),
                                content: document.getElementById("content").value.trim()
                            };

                            if (shortNote) payload.content = shortNote + " - " + payload.content;

                            if (!payload.title || !payload.imageUrl || !payload.content) {
                                showMessage("Nhap day du tieu de, anh va noi dung.");
                                return;
                            }

                            const method = id ? "PUT" : "POST";
                            const url = id ? `${api}/${id}` : api;
                            await fetch(url, {
                                method,
                                headers: { "Content-Type": "application/json" },
                                body: JSON.stringify(payload)
                            });
                            showMessage(id ? "Cap nhat thanh cong." : "Tao bai viet thanh cong.");
                            resetForm();
                            await loadArticles();
                        }

                        async function deleteArticle(id) {
                            await fetch(`${api}/${id}`, { method: "DELETE" });
                            showMessage("Da xoa bai viet #" + id);
                            await loadArticles();
                        }

                        function showMessage(msg) {
                            document.getElementById("message").textContent = msg;
                        }

                        loadArticles();
                    </script>
                </body>
                </html>
                """;
    }

    @GetMapping("/api/articles")
    public List<NewsArticle> getAll() {
        List<NewsArticle> list = new ArrayList<>(store.values());
        list.sort(Comparator.comparingLong(NewsArticle::getId));
        return list;
    }

    @GetMapping("/api/articles/{id}")
    public ResponseEntity<NewsArticle> getById(@PathVariable Long id) {
        NewsArticle article = store.get(id);
        if (article == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(article);
    }

    @PostMapping("/api/articles")
    public NewsArticle create(@RequestBody NewsArticle payload) {
        Long id = idCounter.incrementAndGet();
        NewsArticle article = new NewsArticle(
                id,
                payload.getTitle(),
                payload.getCategory(),
                payload.getImageUrl(),
                payload.getContent()
        );
        store.put(id, article);
        return article;
    }

    @PutMapping("/api/articles/{id}")
    public ResponseEntity<NewsArticle> update(@PathVariable Long id, @RequestBody NewsArticle payload) {
        NewsArticle current = store.get(id);
        if (current == null) {
            return ResponseEntity.notFound().build();
        }
        current.setTitle(payload.getTitle());
        current.setCategory(payload.getCategory());
        current.setImageUrl(payload.getImageUrl());
        current.setContent(payload.getContent());
        store.put(id, current);
        return ResponseEntity.ok(current);
    }

    @DeleteMapping("/api/articles/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        NewsArticle removed = store.remove(id);
        if (removed == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    private void seedData() {
        createSeed(
                "Tong hop tin nong hom nay",
                "Bao va tin tuc",
                "https://images.unsplash.com/photo-1504711434969-e33886168f5c",
                "Cap nhat nhanh tin thoi su trong ngay voi nhieu diem nhan dang chu y."
        );
        createSeed(
                "AI thay doi nganh cong nghe nhu the nao",
                "Cong nghe",
                "https://images.unsplash.com/photo-1518770660439-4636190af475",
                "Tri tue nhan tao dang tang toc tu lap trinh den van hanh doanh nghiep."
        );
        createSeed(
                "Bi kip noi chuyen tu tin",
                "Cach tan gai",
                "https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e",
                "Tap trung ton trong, lang nghe va giu thai do chan thanh khi giao tiep."
        );
        createSeed(
                "3 bai tap de sut manh hon",
                "Cach da bong",
                "https://images.unsplash.com/photo-1574629810360-7efbbe195018",
                "Tap co chan, ky thuat dat tru va canh cham bong de sut luc tot hon."
        );
        createSeed(
                "Cach cam vot cau long dung",
                "Cach choi cau long",
                "https://images.unsplash.com/photo-1626224583764-f87db24ac4ea",
                "Sua grip dung giup pha tan cong nhanh va linh hoat hon trong moi pha bong."
        );
    }

    private void createSeed(String title, String category, String imageUrl, String content) {
        Long id = idCounter.incrementAndGet();
        store.put(id, new NewsArticle(id, title, category, imageUrl, content));
    }
}
