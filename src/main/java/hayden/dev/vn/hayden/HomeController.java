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
}
