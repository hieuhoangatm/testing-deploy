package hayden.dev.vn.hayden;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String home() {
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Hayden Demo</title>
                    <style>
                        body {
                            margin: 0;
                            min-height: 100vh;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            background: linear-gradient(135deg, #0f172a, #1e293b);
                            font-family: Arial, sans-serif;
                            color: #f8fafc;
                        }
                        .card {
                            max-width: 920px;
                            margin: 24px;
                            padding: 28px;
                            border-radius: 16px;
                            background: rgba(15, 23, 42, 0.75);
                            border: 1px solid rgba(148, 163, 184, 0.35);
                            box-shadow: 0 18px 40px rgba(2, 6, 23, 0.45);
                            text-align: center;
                            line-height: 1.6;
                            font-size: 1.2rem;
                            font-weight: 700;
                        }
                    </style>
                </head>
                <body>
                    <div class="card">
                        Hello tất cả mọi người tôi là đại tướng quân đội nhân dân, tổng tham mưu, bộ trưởng bộ quốc phòng,
                        tổng tư lệnh quân khu 1, đại đô đốc lục binh và phòng không Hoàng Văn Minh Hiếu
                    </div>
                </body>
                </html>
                """;
    }

    @GetMapping(value = "/quiz", produces = MediaType.TEXT_HTML_VALUE)
    public String quiz() {
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Quiz Demo</title>
                    <style>
                        * { box-sizing: border-box; }
                        body {
                            margin: 0;
                            min-height: 100vh;
                            display: grid;
                            place-items: center;
                            background: radial-gradient(circle at top, #1d4ed8, #0f172a 60%);
                            font-family: Arial, sans-serif;
                            color: #e2e8f0;
                        }
                        .quiz-box {
                            width: min(860px, 92vw);
                            background: rgba(15, 23, 42, 0.85);
                            border: 1px solid rgba(148, 163, 184, 0.35);
                            border-radius: 18px;
                            padding: 28px;
                            box-shadow: 0 20px 45px rgba(2, 6, 23, 0.5);
                        }
                        h1 {
                            margin: 0 0 18px;
                            font-size: 1.4rem;
                            line-height: 1.5;
                        }
                        .answers {
                            display: grid;
                            grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
                            gap: 12px;
                            margin-top: 12px;
                        }
                        button {
                            border: 0;
                            border-radius: 12px;
                            padding: 14px;
                            font-size: 1rem;
                            font-weight: 700;
                            cursor: pointer;
                            background: linear-gradient(135deg, #22d3ee, #3b82f6);
                            color: #0b1020;
                            transition: transform .12s ease, filter .12s ease;
                        }
                        button:hover { transform: translateY(-2px); filter: brightness(1.06); }
                        .result {
                            margin-top: 20px;
                            min-height: 52px;
                            border-radius: 12px;
                            padding: 14px;
                            background: rgba(30, 41, 59, 0.8);
                            border: 1px dashed rgba(148, 163, 184, 0.45);
                            font-size: 1.05rem;
                            font-weight: 700;
                        }
                    </style>
                </head>
                <body>
                    <section class="quiz-box">
                        <h1>Lê Văn Việt thích làm gì với Trần Hà Linh nhất?</h1>
                        <div class="answers">
                            <button onclick="showResult('A')">A: tắm</button>
                            <button onclick="showResult('B')">B: ngủ</button>
                            <button onclick="showResult('C')">C: vỗ mông</button>
                            <button onclick="showResult('D')">D: cưới</button>
                        </div>
                        <div class="result" id="result">Chọn một đáp án để xem kết quả.</div>
                    </section>

                    <script>
                        function showResult(choice) {
                            const result = document.getElementById('result');
                            result.textContent = 'Bạn chọn ' + choice + '. Việt thích tất cả cơ';
                        }
                    </script>
                </body>
                </html>
                """;
    }
}
