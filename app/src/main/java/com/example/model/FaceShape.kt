package com.example.model

enum class FaceShape(
    val displayName: String,
    val description: String,
    val flatteringTips: String,
    val bestStylesSummary: String
) {
    OVAL(
        displayName = "Oval",
        description = "Balanced proportions with a slightly narrower jawline than temples.",
        flatteringTips = "Considered the most versatile face shape. Almost every hairstyle works well, especially textured crops, layered lobs, and classic parts.",
        bestStylesSummary = "Textured Crop, Curtains, Long Layers, Bob Cut, Pompadour"
    ),
    ROUND(
        displayName = "Round",
        description = "Equal width and length with soft curves and full cheeks.",
        flatteringTips = "Add height and vertical volume on top while keeping the sides tapered to elongate and slim facial contours.",
        bestStylesSummary = "High Fade, Pompadour, Quiff, Wolf Cut, Pixie with Volume"
    ),
    SQUARE(
        displayName = "Square",
        description = "Strong angular jawline with proportional forehead and cheekbones.",
        flatteringTips = "Soft layers, textured fringe, and side-swept waves soften angular features without hiding the chiselled jawline.",
        bestStylesSummary = "Textured Crop, French Crop, Butterfly Cut, Beach Waves, Side Part"
    ),
    OBLONG(
        displayName = "Oblong / Rectangular",
        description = "Longer facial length with uniform width at forehead, cheekbones, and jaw.",
        flatteringTips = "Avoid extreme volume on top. Opt for side volume, curtain bangs, fringes, or medium waves to balance proportions.",
        bestStylesSummary = "Curtains, Shoulder-Length Cut, Butterfly Cut, Low Fade, Modern Mullet"
    ),
    HEART(
        displayName = "Heart",
        description = "Wider forehead tapering down to a delicate, pointed chin.",
        flatteringTips = "Styles with volume around the jawline and chin, such as chin-length bobs, mid fades, and soft curtain bangs, create harmony.",
        bestStylesSummary = "Bob Cut, Korean Curtains, Mid Fade, Long Straight Hair, Shag Cut"
    ),
    DIAMOND(
        displayName = "Diamond",
        description = "Prominent high cheekbones with narrower forehead and jawline.",
        flatteringTips = "Side-parted styles, wispy bangs, and medium layered waves highlight high cheekbones while softening width.",
        bestStylesSummary = "Side Part, Textured Quiff, Butterfly Cut, Curtain Bangs, Curly Layers"
    )
}
