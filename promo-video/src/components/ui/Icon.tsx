import React from "react";

export type IconName =
  | "back"
  | "search"
  | "bell"
  | "phone"
  | "book"
  | "calendar"
  | "clock"
  | "star"
  | "settings"
  | "check"
  | "chevronDown"
  | "chevronRight"
  | "edit"
  | "trash"
  | "send"
  | "whatsapp"
  | "plus"
  | "close"
  | "users"
  | "layers"
  | "offline"
  | "grid"
  | "list"
  | "arrowRight"
  | "download"
  | "eye"
  | "wifi"
  | "battery"
  | "signal"
  | "pin"
  | "document"
  | "image"
  | "sparkle"
  | "home";

interface IconProps {
  name: IconName;
  size?: number;
  color?: string;
  strokeWidth?: number;
  filled?: boolean;
  style?: React.CSSProperties;
}

export const Icon: React.FC<IconProps> = ({
  name,
  size = 24,
  color = "currentColor",
  strokeWidth = 1.8,
  filled = false,
  style,
}) => {
  const common = {
    fill: "none",
    stroke: color,
    strokeWidth,
    strokeLinecap: "round" as const,
    strokeLinejoin: "round" as const,
  };

  const content = (() => {
    switch (name) {
      case "back":
        return <path d="M15 4.5 7.5 12l7.5 7.5" {...common} />;
      case "chevronRight":
        return <path d="m9 5 7 7-7 7" {...common} />;
      case "chevronDown":
        return <path d="m6 9 6 6 6-6" {...common} />;
      case "search":
        return (
          <>
            <circle cx="10.6" cy="10.6" r="6.4" {...common} />
            <path d="m15.4 15.4 5 5" {...common} />
          </>
        );
      case "bell":
        return (
          <>
            <path
              d="M6.2 17.2h11.6l-1.2-2.3V10a4.6 4.6 0 0 0-9.2 0v4.9l-1.2 2.3Z"
              {...common}
            />
            <path d="M10 19.6a2.3 2.3 0 0 0 4 0" {...common} />
          </>
        );
      case "phone":
        return (
          <path
            d="M5.2 3.8h3l1.5 3.9-1.9 1.4a13.6 13.6 0 0 0 6.1 6.1l1.4-1.9 3.9 1.5v3a2 2 0 0 1-2.2 2A17.4 17.4 0 0 1 3.2 6a2 2 0 0 1 2-2.2Z"
            {...common}
          />
        );
      case "book":
        return (
          <>
            <path
              d="M3.8 5.3A2.3 2.3 0 0 1 6.1 3h5.1v17H6.1a2.3 2.3 0 0 0-2.3 2V5.3Z"
              {...common}
            />
            <path
              d="M20.2 5.3A2.3 2.3 0 0 0 17.9 3h-5.1v17h5.1a2.3 2.3 0 0 1 2.3 2V5.3Z"
              {...common}
            />
          </>
        );
      case "calendar":
        return (
          <>
            <rect x="3.4" y="5.2" width="17.2" height="15.4" rx="2.6" {...common} />
            <path d="M3.4 10h17.2M8.2 3v4.2M15.8 3v4.2" {...common} />
            <path d="M7.4 13.6h2M11 13.6h2M14.6 13.6h2M7.4 17h2M11 17h2" {...common} />
          </>
        );
      case "clock":
        return (
          <>
            <circle cx="12" cy="12" r="8.6" {...common} />
            <path d="M12 7.2V12l3.1 2" {...common} />
          </>
        );
      case "star":
        return (
          <path
            d="m12 3 2.8 5.7 6.2.9-4.5 4.4 1.1 6.2-5.6-2.9-5.6 2.9 1.1-6.2L3 9.6l6.2-.9L12 3Z"
            fill={filled ? color : "none"}
            stroke={color}
            strokeWidth={strokeWidth}
            strokeLinejoin="round"
          />
        );
      case "settings":
        return (
          <>
            <circle cx="12" cy="12" r="3.1" {...common} />
            <path
              d="M12 2.8v2.4M12 18.8v2.4M21.2 12h-2.4M5.2 12H2.8M18.5 5.5l-1.7 1.7M7.2 16.8l-1.7 1.7M18.5 18.5l-1.7-1.7M7.2 7.2 5.5 5.5"
              {...common}
            />
          </>
        );
      case "check":
        return <path d="m5 12.4 4.4 4.4L19 7.2" {...common} />;
      case "edit":
        return (
          <>
            <path d="m4.5 19.5 4.2-1 10-10a2.1 2.1 0 0 0-3-3l-10 10-1.2 4Z" {...common} />
            <path d="m13.8 6.6 3.6 3.6" {...common} />
          </>
        );
      case "trash":
        return (
          <>
            <path d="M4.5 6.8h15M9 3.8h6M6.8 6.8l.8 14.2h8.8l.8-14.2" {...common} />
            <path d="M10 11v6M14 11v6" {...common} />
          </>
        );
      case "send":
        return (
          <>
            <path d="M3.6 11.4 20.8 4l-7.4 17-2.5-7.3-7.3-2.3Z" {...common} />
            <path d="M10.9 13.7 20.8 4" {...common} />
          </>
        );
      case "whatsapp":
        return (
          <path
            d="M12 2.2a9.9 9.9 0 0 0-8.5 15L2.1 22l4.9-1.3A9.9 9.9 0 1 0 12 2.2Zm0 18a8.1 8.1 0 0 1-4.1-1.1l-.3-.2-3 .8.8-2.9-.2-.3A8.1 8.1 0 1 1 12 20.2Zm4.6-6c-.2-.1-1.4-.7-1.7-.8-.2-.1-.4-.1-.6.1-.2.2-.6.8-.8 1-.1.2-.3.2-.5.1a6.7 6.7 0 0 1-2-1.2 7.4 7.4 0 0 1-1.4-1.8c-.1-.2 0-.4.1-.5l.4-.5c.2-.2.2-.4.3-.6.1-.2 0-.4 0-.5l-.8-1.9c-.2-.5-.4-.4-.6-.4h-.5c-.2 0-.5.1-.7.3-.2.2-.9.9-.9 2.2 0 1.3.9 2.5 1 2.7.1.2 1.7 2.7 4.2 3.7 2.1.9 2.5.7 3 .7.5 0 1.5-.6 1.7-1.2.2-.6.2-1.1.1-1.2 0-.1-.2-.1-.4-.2Z"
            fill={color}
          />
        );
      case "plus":
        return <path d="M12 5v14M5 12h14" {...common} />;
      case "close":
        return <path d="M6 6l12 12M18 6 6 18" {...common} />;
      case "users":
        return (
          <>
            <circle cx="9" cy="8.2" r="3.4" {...common} />
            <path d="M2.8 20.2a6.2 6.2 0 0 1 12.4 0" {...common} />
            <path d="M15.4 5.3a3.4 3.4 0 0 1 0 6.6M17.4 14.7a6.2 6.2 0 0 1 3.8 5.5" {...common} />
          </>
        );
      case "layers":
        return (
          <>
            <path d="m12 3 9 4.7-9 4.7-9-4.7L12 3Z" {...common} />
            <path d="m4.4 12 7.6 4 7.6-4M4.4 16.3l7.6 4 7.6-4" {...common} />
          </>
        );
      case "offline":
        return (
          <>
            <path d="M4.5 9.5a11.7 11.7 0 0 1 15 0M7.5 13a7 7 0 0 1 4.5-1.5M10.4 16.4a3.4 3.4 0 0 1 3.2-.1" {...common} />
            <path d="M3.5 3.5 20.5 20.5" {...common} />
          </>
        );
      case "grid":
        return (
          <>
            <rect x="3.6" y="3.6" width="7" height="7" rx="1.6" {...common} />
            <rect x="13.4" y="3.6" width="7" height="7" rx="1.6" {...common} />
            <rect x="3.6" y="13.4" width="7" height="7" rx="1.6" {...common} />
            <rect x="13.4" y="13.4" width="7" height="7" rx="1.6" {...common} />
          </>
        );
      case "list":
        return (
          <>
            <path d="M8.5 6.5h12M8.5 12h12M8.5 17.5h12" {...common} />
            <circle cx="4" cy="6.5" r="1" fill={color} />
            <circle cx="4" cy="12" r="1" fill={color} />
            <circle cx="4" cy="17.5" r="1" fill={color} />
          </>
        );
      case "arrowRight":
        return (
          <>
            <path d="M4 12h16M14 6l6 6-6 6" {...common} />
          </>
        );
      case "download":
        return (
          <>
            <path d="M12 3.5v11M7.5 10.5 12 15l4.5-4.5" {...common} />
            <path d="M4 17.5v1.7A2.3 2.3 0 0 0 6.3 21.5h11.4a2.3 2.3 0 0 0 2.3-2.3v-1.7" {...common} />
          </>
        );
      case "eye":
        return (
          <>
            <path d="M2.6 12s3.4-6.3 9.4-6.3S21.4 12 21.4 12s-3.4 6.3-9.4 6.3S2.6 12 2.6 12Z" {...common} />
            <circle cx="12" cy="12" r="2.7" {...common} />
          </>
        );
      case "wifi":
        return (
          <>
            <path d="M3.8 8.5a12.6 12.6 0 0 1 16.4 0M7 12.1a8 8 0 0 1 10 0M10.2 15.7a3.5 3.5 0 0 1 3.6 0" {...common} />
            <circle cx="12" cy="19" r="1.1" fill={color} />
          </>
        );
      case "battery":
        return (
          <>
            <rect x="2.8" y="7.5" width="16.4" height="9" rx="2.2" {...common} />
            <path d="M22 10.4v3.2" {...common} strokeWidth={2.4} />
            <rect x="5" y="9.7" width="10.6" height="4.6" rx="1" fill={color} stroke="none" />
          </>
        );
      case "signal":
        return (
          <>
            <path d="M4 18.5v-2M8.7 18.5v-5M13.4 18.5v-8M18.1 18.5V7.4" {...common} strokeWidth={2.6} />
          </>
        );
      case "pin":
        return (
          <>
            <path d="M12 21.2s6.2-5.7 6.2-10.4a6.2 6.2 0 1 0-12.4 0C5.8 15.5 12 21.2 12 21.2Z" {...common} />
            <circle cx="12" cy="10.6" r="2.2" {...common} />
          </>
        );
      case "document":
        return (
          <>
            <path d="M6 2.8h7.4L19.4 9v12.2H6V2.8Z" {...common} />
            <path d="M13.4 2.8V9h6M9 13h7M9 16.5h7" {...common} />
          </>
        );
      case "image":
        return (
          <>
            <rect x="3.4" y="4.4" width="17.2" height="15.2" rx="2.4" {...common} />
            <circle cx="8.6" cy="9.4" r="1.7" {...common} />
            <path d="m4.5 17.5 5-5 4.4 4.4 2.6-2.6 3 3" {...common} />
          </>
        );
      case "sparkle":
        return (
          <path
            d="M12 3.4c.5 4.4 2.2 6.1 6.6 6.6-4.4.5-6.1 2.2-6.6 6.6-.5-4.4-2.2-6.1-6.6-6.6 4.4-.5 6.1-2.2 6.6-6.6ZM18.4 15.2c.3 2.2 1.2 3.1 3.4 3.4-2.2.3-3.1 1.2-3.4 3.4-.3-2.2-1.2-3.1-3.4-3.4 2.2-.3 3.1-1.2 3.4-3.4Z"
            fill={filled ? color : "none"}
            stroke={color}
            strokeWidth={strokeWidth}
            strokeLinejoin="round"
          />
        );
      case "home":
        return (
          <>
            <path d="m3.5 11 8.5-7 8.5 7v9.2h-6V13h-5v7.2h-6V11Z" {...common} />
          </>
        );
      default:
        return null;
    }
  })();

  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      style={{ display: "block", flexShrink: 0, ...style }}
      aria-hidden="true"
    >
      {content}
    </svg>
  );
};
