from pathlib import Path

from reportlab.lib import colors
from reportlab.lib.enums import TA_LEFT, TA_RIGHT
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import mm
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import (
    BaseDocTemplate,
    Frame,
    HRFlowable,
    Image,
    PageBreak,
    PageTemplate,
    Paragraph,
    Spacer,
    Table,
    TableStyle,
    KeepTogether,
)


ROOT = Path(r"C:\Users\chlqn\Desktop\shop")
OUT = ROOT / "output" / "resume" / "최태준_백엔드개발자_이력서.pdf"
PHOTO = Path(r"C:\Users\chlqn\Desktop\증명사진_최태준.jpg")

pdfmetrics.registerFont(TTFont("Malgun", r"C:\Windows\Fonts\malgun.ttf"))
pdfmetrics.registerFont(TTFont("Malgun-Bold", r"C:\Windows\Fonts\malgunbd.ttf"))

NAVY = colors.HexColor("#17365D")
BLUE = colors.HexColor("#2F75B5")
LIGHT_BLUE = colors.HexColor("#EAF2F8")
PALE = colors.HexColor("#F4F6F8")
GRAY = colors.HexColor("#666666")
DARK = colors.HexColor("#222222")
BORDER = colors.HexColor("#D9E2F3")

PAGE_W, PAGE_H = A4
LEFT = 17 * mm
RIGHT = 17 * mm
TOP = 13 * mm
BOTTOM = 13 * mm
CONTENT_W = PAGE_W - LEFT - RIGHT


styles = getSampleStyleSheet()

body = ParagraphStyle(
    "BodyKR",
    parent=styles["BodyText"],
    fontName="Malgun",
    fontSize=8.7,
    leading=11.1,
    textColor=DARK,
    spaceAfter=2.2,
    wordWrap="CJK",
)
body_gray = ParagraphStyle(
    "BodyGray",
    parent=body,
    fontSize=8.4,
    leading=10.7,
    textColor=GRAY,
    spaceAfter=3,
)
title = ParagraphStyle(
    "TitleKR",
    parent=body,
    fontName="Malgun-Bold",
    fontSize=22,
    leading=24,
    textColor=NAVY,
    spaceAfter=1,
)
subtitle = ParagraphStyle(
    "SubtitleKR",
    parent=body,
    fontName="Malgun-Bold",
    fontSize=10,
    leading=12,
    textColor=BLUE,
    spaceAfter=6,
)
contact = ParagraphStyle(
    "ContactKR",
    parent=body,
    fontSize=8.1,
    leading=10.3,
    textColor=GRAY,
    spaceAfter=1,
)
section = ParagraphStyle(
    "SectionKR",
    parent=body,
    fontName="Malgun-Bold",
    fontSize=11.5,
    leading=13,
    textColor=NAVY,
    spaceBefore=5.5,
    spaceAfter=2,
    keepWithNext=True,
)
role = ParagraphStyle(
    "RoleKR",
    parent=body,
    fontName="Malgun-Bold",
    fontSize=9.5,
    leading=11.8,
    textColor=DARK,
    spaceBefore=2,
    spaceAfter=1,
    keepWithNext=True,
)
bullet = ParagraphStyle(
    "BulletKR",
    parent=body,
    fontSize=8.45,
    leading=10.6,
    leftIndent=13,
    firstLineIndent=-7,
    bulletIndent=1,
    spaceAfter=1.8,
)
label = ParagraphStyle(
    "LabelKR",
    parent=body,
    fontName="Malgun-Bold",
    fontSize=8.1,
    leading=9.5,
    textColor=NAVY,
    spaceAfter=0,
)
value = ParagraphStyle(
    "ValueKR",
    parent=body,
    fontSize=8.15,
    leading=9.7,
    textColor=DARK,
    spaceAfter=0,
)


def footer(canvas, doc):
    canvas.saveState()
    canvas.setStrokeColor(BORDER)
    canvas.setLineWidth(0.4)
    canvas.line(LEFT, 9.5 * mm, PAGE_W - RIGHT, 9.5 * mm)
    canvas.setFont("Malgun", 7.2)
    canvas.setFillColor(GRAY)
    canvas.drawString(LEFT, 6.4 * mm, "최태준 | Backend Developer")
    canvas.drawRightString(PAGE_W - RIGHT, 6.4 * mm, f"{doc.page} / 1")
    canvas.restoreState()


def section_heading(text):
    return KeepTogether([
        Paragraph(text, section),
        HRFlowable(width="100%", thickness=1.0, color=BLUE, spaceBefore=0, spaceAfter=3.5),
    ])


def bullet_p(text):
    return Paragraph(text, bullet, bulletText="•")


def role_header(name, meta, link=None):
    suffix = f"  <font color='#666666' size='8'>|  {meta}</font>"
    if link:
        suffix += f"  <font color='#666666' size='8'>|</font>  <link href='{link}' color='#2F75B5'><u>GitHub</u></link>"
    return Paragraph(name + suffix, role)


doc = BaseDocTemplate(
    str(OUT),
    pagesize=A4,
    leftMargin=LEFT,
    rightMargin=RIGHT,
    topMargin=TOP,
    bottomMargin=BOTTOM,
    title="최태준 백엔드 개발자 이력서",
    author="최태준",
    subject="Java Spring Backend Developer Resume",
)
frame = Frame(LEFT, BOTTOM, CONTENT_W, PAGE_H - TOP - BOTTOM, id="resume")
doc.addPageTemplates(PageTemplate(id="resume-template", frames=[frame], onPage=footer))

story = []

header_text = [
    Paragraph("최태준", title),
    Paragraph("BACKEND DEVELOPER", subtitle),
    Paragraph("010-6866-9066  |  <link href='mailto:chlqnftkwh@naver.com' color='#2F75B5'>chlqnftkwh@naver.com</link>", contact),
    Paragraph(
        "<link href='https://github.com/chtj1024' color='#2F75B5'>GitHub</link>  |  "
        "<link href='https://app.notion.com/p/3c47b079826d81afa1f3f11ad400a673' color='#2F75B5'>Portfolio</link>  |  "
        "<link href='https://noahchoi.tistory.com/' color='#2F75B5'>Blog</link>",
        contact,
    ),
]
photo = Image(str(PHOTO), width=29 * mm, height=38.7 * mm)
photo.hAlign = "CENTER"
header = Table([[header_text, photo]], colWidths=[CONTENT_W - 35 * mm, 35 * mm], hAlign="LEFT")
header.setStyle(TableStyle([
    ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
    ("LEFTPADDING", (0, 0), (-1, -1), 0),
    ("RIGHTPADDING", (0, 0), (-1, -1), 0),
    ("TOPPADDING", (0, 0), (-1, -1), 0),
    ("BOTTOMPADDING", (0, 0), (-1, -1), 0),
]))
story.append(header)

story.append(section_heading("PROFILE"))
story.append(Paragraph(
    "주문·결제 과정의 데이터 정합성을 고민하는 백엔드 개발자입니다. Spring Boot와 JPA로 이커머스 서비스를 구현하며 "
    "조건부 UPDATE와 멱등성 있는 상태 전이로 동시 주문, 결제 실패 및 주문 만료 상황을 처리했습니다. "
    "핵심 비즈니스 규칙은 실제 MySQL 기반 통합·동시성 테스트로 검증했습니다.",
    body,
))

story.append(section_heading("TECHNICAL SKILLS"))
skills_data = [
    [Paragraph("Backend", label), Paragraph("Java 21 · Spring Boot 3.5 · Spring Data JPA · QueryDSL", value)],
    [Paragraph("Security / API", label), Paragraph("Spring Security · JWT · Swagger / OpenAPI", value)],
    [Paragraph("Database / Test", label), Paragraph("MySQL 8 · JUnit 5 · Testcontainers", value)],
    [Paragraph("Frontend / Tools", label), Paragraph("React 19 · TypeScript · Vite · Axios · Git · Gradle · Docker · Codex (AI-assisted development)", value)],
]
skills = Table(skills_data, colWidths=[35 * mm, CONTENT_W - 35 * mm], repeatRows=0)
skills.setStyle(TableStyle([
    ("BACKGROUND", (0, 0), (0, -1), LIGHT_BLUE),
    ("GRID", (0, 0), (-1, -1), 0.35, BORDER),
    ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
    ("LEFTPADDING", (0, 0), (-1, -1), 5),
    ("RIGHTPADDING", (0, 0), (-1, -1), 5),
    ("TOPPADDING", (0, 0), (-1, -1), 3),
    ("BOTTOMPADDING", (0, 0), (-1, -1), 3),
]))
story.append(skills)

story.append(section_heading("PROJECT EXPERIENCE"))
story.append(role_header(
    "Shop - 주문·결제 데이터 정합성을 고려한 이커머스 서비스",
    "1인 프로젝트 · 2026.04 - 2026.07",
    "https://github.com/chtj1024/E-commerce_Project",
))
story.append(Paragraph(
    "회원·상품·장바구니·주문·결제 흐름을 구현하고, 동시 주문과 결제 상태 경합에서 발생하는 데이터 정합성 문제를 중점적으로 해결했습니다.",
    body_gray,
))
story.extend([
    bullet_p("상품 상태와 남은 재고를 조건으로 하는 원자적 UPDATE를 적용해 재고 초과 판매를 방지했습니다."),
    bullet_p("복수 상품을 ID 순서로 차감해 자원 점유 순서를 고정하고, 일부 상품 실패 시 전체 재고 차감과 주문 생성을 롤백했습니다."),
    bullet_p("PAYMENT_PENDING 상태의 조건부 전이로 결제 성공·실패·만료의 중복 처리와 재고 중복 복구를 방지했습니다."),
    bullet_p("30초 주기·최대 100건의 만료 주문 처리와 status + expiresAt 복합 인덱스를 적용하고, 개별 실패를 격리했습니다."),
    bullet_p("Testcontainers MySQL 8.4에서 재고 10개 상품에 20개 동시 주문을 실행해 성공 주문 10건과 최종 재고 0개를 검증했습니다."),
    bullet_p("QueryDSL 복합 검색과 JWT Access·Refresh Token 분리, Refresh Token Rotation 및 역할 기반 인가를 구현했습니다."),
])

story.append(Spacer(1, 4))

story.append(section_heading("OTHER PROJECTS"))
story.append(role_header("출산율 미래 데이터 예측", "1인 프로젝트 · 2022.06 - 2022.12", "https://github.com/chtj1024/BirthRate_Forecast"))
story.append(bullet_p("공공데이터를 수집·전처리하고 EDA를 수행한 뒤 ARIMA 시계열 모델로 출산율을 예측하고 결과를 시각화했습니다."))
story.append(Spacer(1, 2))
story.append(role_header("웹·데스크톱 캘린더", "1인 프로젝트 · 2023.06 - 2023.12", "https://github.com/chtj1024/Calendar_Project"))
story.append(bullet_p("React와 Electron으로 일정 CRUD, 드래그 기반 일정 등록, Tray 실행 및 사용자 설정 저장 기능을 구현했습니다."))

story.append(section_heading("ADDITIONAL EXPERIENCE"))
story.append(role_header("포트나이트 프로게이머 · 인게임 리더", "루나틱하이 · Cloud9 · 1년 6개월"))
story.append(Paragraph(
    "듀오와 스쿼드 경기에서 인게임 리더를 맡아 실시간 상황 판단, 팀 전략 조율, 경기 분석과 반복 피드백을 수행했습니다.",
    body_gray,
))
story.extend([
    bullet_p("중국 DouYu 주최 듀오 대회 2위(상금 800만 원)로 상하이 Stan Lee's Comic Universe Fortnite Open 시드를 획득해 출전했습니다."),
    bullet_p("인벤 주최 솔로 대회 80명 중 종합 2위, 듀오 대회 30팀 중 종합 2위를 기록했습니다."),
    bullet_p("Cloud9 소속 프로게이머로 부산 G-STAR에 초청되어 인플루언서들과 이벤트 경기에 참여했습니다."),
])

story.append(section_heading("EDUCATION"))
story.append(role_header("인하공업전문대학교 컴퓨터정보과", "인천"))
story.append(bullet_p("학사학위 전공심화과정 졸업 · 2024"))
story.append(bullet_p("전문학사 졸업 · 2023"))

story.append(section_heading("ADDITIONAL INFORMATION"))
info_data = [[
    Paragraph("<b>병역</b>  군필", value),
    Paragraph("<b>지원 가능 시점</b>  즉시", value),
]]
info = Table(info_data, colWidths=[CONTENT_W / 2, CONTENT_W / 2])
info.setStyle(TableStyle([
    ("BACKGROUND", (0, 0), (-1, -1), PALE),
    ("BOX", (0, 0), (-1, -1), 0.35, BORDER),
    ("INNERGRID", (0, 0), (-1, -1), 0.35, BORDER),
    ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
    ("LEFTPADDING", (0, 0), (-1, -1), 6),
    ("RIGHTPADDING", (0, 0), (-1, -1), 6),
    ("TOPPADDING", (0, 0), (-1, -1), 5),
    ("BOTTOMPADDING", (0, 0), (-1, -1), 5),
]))
story.append(info)

OUT.parent.mkdir(parents=True, exist_ok=True)
doc.build(story)
print(OUT)
