type JwtPayload = {
  role?: string;
};

export const hasAdminRole = (accessToken: string | null): boolean => {
  if (!accessToken) return false;

  try {
    const encodedPayload = accessToken.split(".")[1];
    if (!encodedPayload) return false;

    const base64 = encodedPayload
      .replace(/-/g, "+")
      .replace(/_/g, "/")
      .padEnd(Math.ceil(encodedPayload.length / 4) * 4, "=");
    const payload = JSON.parse(atob(base64)) as JwtPayload;

    return payload.role === "ADMIN";
  } catch {
    return false;
  }
};
