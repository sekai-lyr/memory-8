---
name: SEKAI Study Workspace
description: A focused Chinese first English vocabulary workspace.
colors:
  primary: "#72e4c6"
  primary-deep: "#55c9b0"
  primary-soft: "#a7f5df"
  support-blue: "#8db5d5"
  support-coral: "#f0a39a"
  support-violet: "#b4acd4"
  support-amber: "#e7bd7b"
  feedback-red: "#ff9a91"
  background: "#090f1c"
  surface: "rgba(16, 27, 45, .9)"
  surface-alt: "rgba(19, 32, 51, .92)"
  input: "rgba(6, 13, 25, .66)"
  border: "rgba(213, 232, 227, .14)"
  border-hover: "rgba(114, 228, 198, .52)"
  text: "#edf4f1"
  text-dim: "#a9b8bf"
  text-faint: "#758790"
  ink: "#07131a"
typography:
  display:
    fontFamily: "Avenir Next, PingFang SC, Noto Sans SC, Microsoft YaHei, system-ui, sans-serif"
    fontSize: "clamp(1.9rem, 3.2vw, 3rem)"
    fontWeight: 700
    lineHeight: 1.08
    letterSpacing: "-.025em"
  headline:
    fontFamily: "Avenir Next, PingFang SC, Noto Sans SC, Microsoft YaHei, system-ui, sans-serif"
    fontSize: "1.12rem"
    fontWeight: 750
    lineHeight: 1.25
  title:
    fontFamily: "Avenir Next, PingFang SC, Noto Sans SC, Microsoft YaHei, system-ui, sans-serif"
    fontSize: ".94rem"
    fontWeight: 750
    lineHeight: 1.35
  body:
    fontFamily: "Avenir Next, PingFang SC, Noto Sans SC, Microsoft YaHei, system-ui, sans-serif"
    fontSize: ".9rem"
    fontWeight: 400
    lineHeight: 1.6
  label:
    fontFamily: "ui-monospace, SFMono-Regular, Consolas, monospace"
    fontSize: ".68rem"
    fontWeight: 800
    lineHeight: 1.2
    letterSpacing: ".11em"
rounded:
  sm: "9px"
  md: "12px"
  lg: "16px"
spacing:
  xs: "8px"
  sm: "14px"
  md: "22px"
  lg: "28px"
  xl: "48px"
components:
  button-primary:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.ink}"
    rounded: "{rounded.sm}"
    padding: "9px 15px"
    height: "40px"
  button-primary-hover:
    backgroundColor: "{colors.primary-soft}"
    textColor: "{colors.ink}"
    rounded: "{rounded.sm}"
  button-ghost:
    backgroundColor: "transparent"
    textColor: "{colors.text-dim}"
    rounded: "{rounded.sm}"
    padding: "9px 15px"
    height: "40px"
  input:
    backgroundColor: "{colors.input}"
    textColor: "{colors.text}"
    rounded: "{rounded.sm}"
    padding: "9px 12px"
    height: "42px"
  panel:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.text}"
    rounded: "{rounded.lg}"
    padding: "22px"
---

# Design System: SEKAI Study Workspace

## Overview

Creative North Star: "The Midnight Study Ledger"

SEKAI is a Chinese first English vocabulary workspace for the moment between intention and action. The visual language treats each screen like a quiet study ledger: midnight ink surfaces hold the content, paper like dividers separate decisions, and a single mint signal marks the next useful move.

The system is calm, precise, and slightly editorial. It keeps the existing study atmosphere while removing the old cyber grid, scan lines, and multi color neon treatment. The generated study desk image is used as a restrained anchor for hero surfaces and the practice card, so the product story stays tied to vocabulary study rather than generic productivity imagery.

Key Characteristics:

- Midnight ink base with cool paper text
- One mint signal accent for action, progress, and focus
- Editorial image crop with negative space for copy
- Compact navigation and task first page layouts
- Motion that supports route orientation and yields to reduced motion preferences

The One Signal Rule. Mint is a signal, not wallpaper. Keep it concentrated on primary actions, active navigation, progress, focus rings, and meaningful study status.

## Colors

The palette pairs a deep blue black canvas with cool desaturated text and one high clarity mint accent. Warm colors remain reserved for feedback and supporting study states.

### Primary

- Mint Signal: primary for the next action, active route, progress, and focus.
- Deep Mint: pressed and supporting signal states.
- Soft Mint: hover and high contrast signal state.

### Secondary

- Cool Blue: supporting metadata and quiet information.
- Warm Coral: soft attention and error feedback.
- Muted Violet: secondary membership and learning context.
- Paper Amber: notices, import states, and commercial guidance.

### Neutral

- Ink Field: page background and visual anchor.
- Study Surface: panel and card surface.
- Raised Surface: alternate panel surface for layered content.
- Input Night: field background that keeps forms quiet.
- Cool Paper: primary text.
- Pencil Gray: supporting text.
- Faint Graphite: low priority labels and dividers.
- Ink Text: text placed on the mint signal.

The Accent Rarity Rule. A screen should read clearly in grayscale before mint is added. Mint carries meaning only when it remains scarce.

## Typography

Display Font: Avenir Next, with PingFang SC, Noto Sans SC, Microsoft YaHei, and system UI fallbacks.

Body Font: The same humanist system stack keeps Chinese and English vocabulary in one voice.

Label/Mono Font: ui-monospace, SFMono-Regular, Consolas, monospace for phonetics, timers, and compact status labels.

Character: The type system is direct and quiet. Large headlines create orientation, medium titles group study tasks, and small labels stay informational instead of ornamental.

### Hierarchy

- Display: bold, fluid, tight leading; page and hero titles.
- Headline: strong, compact, and clear; panel headings and study outcomes.
- Title: medium scale; task names, feature names, and supporting modules.
- Body: relaxed leading; explanations, feedback, and instructional copy.
- Label: compact mono or small bold text; phonetic data, timers, table headers, and status chips.

The Sentence First Rule. Use a meaningful Chinese statement for page orientation before adding a category label. Labels support hierarchy but do not carry the story alone.

## Layout

The global page container uses a wide study desk model. Standard pages cap at 1280px, wide data pages can reach 1400px, and the primary two column study layouts reserve a 300px side rail. The navigation is a 72px bar on desktop and becomes 64px on small screens.

Spacing follows a compact rhythm built from 8px, 14px, 22px, and 28px gaps. Hero bars stay short enough to keep the first action above the fold. Summary groups use four columns on large screens, two columns at tablet widths, and one column on small screens.

At 1100px, secondary navigation items collapse. At 860px, main and side content stack and supporting grids reduce to two columns. At 560px, the hero image crop is removed, controls become single column where needed, and page padding narrows to preserve reading width.

## Elevation & Depth

Depth comes from tonal layering and restrained ambient shadows rather than glow. Panels use a darker blue surface over the ink field, thin cool borders show grouping, and shadows are reserved for the navigation, hero, and menu surfaces. There is no animated neon halo.

### Shadow Vocabulary

- Ambient study shadow: 0 18px 44px rgba(2, 7, 16, .22) for primary panels.
- Deep menu shadow: 0 28px 74px rgba(2, 7, 16, .34) for floating navigation content.

The Flat Rest Rule. Surfaces are quiet at rest. Hover changes border, tone, or position slightly and never turns a study screen into a light show.

## Shapes

The form language uses gently rounded rectangles with a 16px panel radius, 12px secondary surface radius, and 9px control radius. Borders are thin and low contrast. Full pill shapes are reserved for badges and compact status chips. Tables and list groups use dividers and internal spacing instead of heavy boxes.

## Components

### Buttons

- Shape: gently rounded controls with a 9px radius.
- Primary: mint signal background with ink text, compact 40px height, and clear task wording.
- Hover and Focus: soften to the light mint state or add a mint focus ring; keep the transformation subtle.
- Secondary and Ghost: dark tonal surface or transparent background with pencil gray text. They support the primary action without competing with it.

### Chips

- Style: compact rounded badges with a faint border and translucent surface.
- State: pro and active study states use mint text and a low opacity mint background. Neutral metadata stays gray.

### Cards / Containers

- Corner Style: large panels use 16px, secondary cards use 12px, controls use 9px.
- Background: layered midnight surfaces with a cool border.
- Shadow Strategy: ambient shadow only for primary surfaces and floating menus.
- Border: cool, low contrast, slightly stronger on hover.
- Internal Padding: 18px to 25px based on content density.

### Inputs / Fields

- Style: quiet dark fields with a cool border, 42px minimum height, and 9px corners.
- Focus: mint border plus a soft mint outline.
- Error and Disabled: coral feedback surface for errors; reduced opacity for disabled controls.

### Navigation

The navigation keeps the SEKAI mark and wordmark on the left, six high value study destinations in the center, and membership plus a compact details menu on the right. Active routes use a low opacity mint surface. Secondary destinations live in the menu so the first view remains focused.

### Hero Image

The study desk artwork anchors the auth surface, page hero bars, and practice card as a background crop with a dark gradient. The image supports the copy and never replaces the action or page title. On small screens, the hero crop is removed when it would compete with task completion.

### Motion

Route entry uses anime.js WAAPI animation on the page root with a short opacity rise and vertical settle. The effect is cleaned up on unmount and skipped when the user requests reduced motion. Existing control feedback uses short transform, color, and border transitions only.

## Do's and Don'ts

### Do:

- Do keep one clear next action visible near the page title.
- Do use mint for meaning: action, active state, progress, focus, or study status.
- Do use the study artwork with a calm crop and a dark copy gradient.
- Do preserve visible labels for Chinese learners, especially on forms and study controls.
- Do let mobile layouts stack content and remove decorative image crops when space is tight.
- Do honor prefers-reduced-motion for route entry and background drift.

### Don't:

- Don't reintroduce scan lines, cyber grids, purple neon, or multi accent glow.
- Don't turn every section into a generic card grid with equal visual weight.
- Don't use category eyebrows as the main page hierarchy.
- Don't place long English copy where a concise Chinese instruction is clearer.
- Don't animate width, height, margin, or padding for interaction feedback.
- Don't use a decorative image that competes with the word, answer, or next study action.
