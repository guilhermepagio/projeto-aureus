# Accessibility Review: Google One Tap Login Flow

## Overview
This review focuses on the accessibility implications of the newly introduced manual Google One Tap prompt over a white Welcome Screen, as defined in `EXPERIENCE.md` and `DESIGN.md`.

## Findings

### 1. Contrast
- **Potential Issue:** The `DESIGN.md` specifies a "clean white background (`color.surface.card` - #FFFFFF)" for the Welcome Screen. If the standard "Entrar com Google" button uses its white variant, there will be insufficient contrast between the button and the background, making it hard to perceive for low-vision users.
- **Recommendation:** Ensure the "Entrar com Google" button uses a high-contrast variant (e.g., the blue background variant or with a clear, high-contrast border) against the white Welcome Screen.

### 2. Focus Trapping & Management
- **Potential Issue (Dismissal):** The `EXPERIENCE.md` mentions focus trapping for Modals and Bottom Sheets, but it does not specify focus management for the Google One Tap overlay. If the user dismisses the prompt (Failure Path), focus must be deliberately restored to the "Entrar com Google" button. 
- **Potential Issue (Success):** Since login transitions to the Consolidação tab "without a hard page reload", keyboard focus might be lost or left in a detached DOM element.
- **Recommendation:** 
  1. Specify that upon dismissal of the One Tap overlay, focus must return to the "Entrar com Google" button.
  2. Specify that upon successful login and view transition, focus should be programmatically moved to a logical starting point in the Consolidação tab (e.g., the main `<h1>` or the first tab navigation element).

### 3. Screen Reader (ARIA) & Notifications
- **Potential Issue:** The Failure Path triggers an Error toast ("Falha ao autenticar. Tente novamente."). It is not explicitly stated if this Toast is accessible to screen readers.
- **Recommendation:** The Error toast must be implemented as an `aria-live` region (e.g., `role="alert"` or `aria-live="assertive"`) so that screen readers announce the failure when the popup closes.
- **Recommendation:** Ensure the "Entrar com Google" button has an appropriate `aria-label` or text content if not already provided by the standard component.

### 4. Keyboard Navigation Implications
- **Potential Issue:** The flow relies on the user navigating to the "Entrar com Google" button, triggering the overlay, and then navigating within the overlay.
- **Recommendation:** Ensure the "Entrar com Google" button is fully operable via the `Enter` and `Space` keys and clearly shows the teal focus ring defined in the Accessibility Floor.
