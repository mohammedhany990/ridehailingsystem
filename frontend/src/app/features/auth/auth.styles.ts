export const AUTH_STYLES = `
  .auth-wrap {
    min-height: 100dvh; display: grid; place-items: center;
    padding: 24px; position: relative; overflow: hidden;
  }
  .glow {
    position: absolute; width: 480px; height: 480px; border-radius: 50%;
    filter: blur(120px); opacity: 0.35; pointer-events: none;
  }
  .glow.a { background: var(--brand); top: -160px; inset-inline-start: -120px; }
  .glow.b { background: var(--accent); bottom: -180px; inset-inline-end: -120px; opacity: 0.22; }
  .card {
    width: 100%; max-width: 400px; position: relative;
    background: var(--bg-elev); border: 1px solid var(--line);
    border-radius: var(--radius-xl); padding: 34px 28px;
    box-shadow: var(--shadow-lg); animation: pop var(--dur-slow) var(--ease-out);
  }
  .logo { font-size: 30px; font-weight: 800; letter-spacing: -0.03em; margin: 0 0 4px; }
  .logo span { color: var(--brand-strong); }
  .subtitle { color: var(--text-dim); margin: 0 0 26px; font-size: 14px; }
  label { display: block; font-size: 12.5px; font-weight: 600; color: var(--text-dim); margin: 0 0 6px 2px; }
  .field { margin-bottom: 16px; }
  input {
    width: 100%; padding: 13px 15px; border-radius: var(--radius-md);
    background: var(--bg); border: 1px solid var(--line); color: var(--text);
    transition: border-color var(--dur-fast), box-shadow var(--dur-fast);
  }
  input:focus { outline: none; border-color: var(--brand); box-shadow: var(--ring); }
  input.invalid { border-color: var(--danger); }
  .hint { font-size: 12px; color: var(--danger); margin: 5px 2px 0; min-height: 14px; }
  .otp-input {
    text-align: center; font-size: 26px; letter-spacing: 12px; font-weight: 700;
    padding: 14px 10px 14px 22px;
  }
  [dir='rtl'] .otp-input { letter-spacing: 12px; padding: 14px 22px 14px 10px; }
  .submit {
    width: 100%; padding: 14px; border: none; border-radius: var(--radius-md);
    background: linear-gradient(135deg, var(--brand), var(--brand-strong));
    color: #fff; font-weight: 700; font-size: 15px; margin-top: 8px;
    transition: transform var(--dur-fast) var(--ease-spring), filter var(--dur-fast);
    box-shadow: 0 10px 26px rgba(79, 124, 255, 0.35);
  }
  .submit:hover:not(:disabled) { transform: translateY(-1px); filter: brightness(1.06); }
  .submit:active:not(:disabled) { transform: scale(0.98); }
  .submit:disabled { opacity: 0.55; cursor: not-allowed; box-shadow: none; }
  .alt { text-align: center; margin-top: 20px; font-size: 13.5px; color: var(--text-dim); }
  .alt a { color: var(--brand-strong); font-weight: 700; text-decoration: none; }
  .error-banner {
    background: var(--danger-soft); color: var(--danger);
    border-radius: var(--radius-sm); padding: 10px 14px; font-size: 13px;
    margin-bottom: 14px; animation: fade-in var(--dur-fast);
  }
  .role-toggle {
    display: flex; gap: 8px; margin-bottom: 16px;
  }
  .role-toggle button {
    flex: 1; padding: 11px; border-radius: var(--radius-md);
    border: 1px solid var(--line); background: var(--bg); color: var(--text-dim);
    font-weight: 600; transition: all var(--dur-fast);
  }
  .role-toggle button.selected {
    background: var(--brand-soft); border-color: var(--brand); color: var(--brand-strong);
  }
`;
