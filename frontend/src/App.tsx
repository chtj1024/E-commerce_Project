import { useEffect, useState, type FormEvent, type ReactNode } from "react";
import axios from "axios";
import "./App.css";

const API_URL = "http://localhost:8080/api/members";
const PASSWORD_MIN_LENGTH = 8;
const PASSWORD_MAX_LENGTH = 20;

type SignupResponse = {
  id: number;
  email: string;
  name: string;
};

type LoginResponse = {
  accessToken: string;
};

type Page = "home" | "signup" | "login";

const getPageFromPath = (): Page => {
  if (window.location.pathname === "/signup") return "signup";
  if (window.location.pathname === "/login") return "login";
  return "home";
};

function App() {
  const [page, setPage] = useState<Page>(getPageFromPath);
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const isLoggedIn = Boolean(accessToken);

  useEffect(() => {
    const handlePopState = () => setPage(getPageFromPath());
    window.addEventListener("popstate", handlePopState);
    return () => window.removeEventListener("popstate", handlePopState);
  }, []);

  useEffect(() => {
    let isMounted = true;

    const refreshAccessToken = async () => {
      try {
        const response = await axios.post<LoginResponse>(
          `${API_URL}/refresh`,
          null,
          { timeout: 5000, withCredentials: true },
        );

        if (isMounted) setAccessToken(response.data.accessToken);
      } catch {
        if (isMounted) setAccessToken(null);
      }
    };

    void refreshAccessToken();
    return () => {
      isMounted = false;
    };
  }, []);

  const navigate = (nextPage: Page) => {
    const path = nextPage === "home" ? "/" : `/${nextPage}`;
    window.history.pushState({}, "", path);
    setPage(nextPage);
  };

  const logout = async () => {
    try {
      await axios.post(`${API_URL}/logout`, null, {
        timeout: 5000,
        withCredentials: true,
      });
    } finally {
      setAccessToken(null);
      navigate("home");
    }
  };

  return (
    <div className="app-shell">
      <header className="site-header">
        <button className="brand" type="button" onClick={() => navigate("home")}>
          shop.
        </button>
        <nav aria-label="주요 메뉴">
          {isLoggedIn ? (
            <button className="text-button" type="button" onClick={logout}>
              로그아웃
            </button>
          ) : (
            <>
              <button className="text-button" type="button" onClick={() => navigate("login")}>
                로그인
              </button>
              <button className="nav-signup" type="button" onClick={() => navigate("signup")}>
                회원가입
              </button>
            </>
          )}
        </nav>
      </header>

      {page === "home" && <Home isLoggedIn={isLoggedIn} onNavigate={navigate} />}
      {page === "signup" && <Signup onNavigate={navigate} />}
      {page === "login" && (
        <Login
          onLogin={(token) => {
            setAccessToken(token);
            navigate("home");
          }}
          onNavigate={navigate}
        />
      )}
    </div>
  );
}

function Home({ isLoggedIn, onNavigate }: { isLoggedIn: boolean; onNavigate: (page: Page) => void }) {
  return (
    <main className="home">
      <section className="hero">
        <p className="eyebrow">EVERYDAY MARKET</p>
        <h1>좋아하는 것을<br />더 가깝게.</h1>
        <p className="hero-copy">일상에 필요한 좋은 물건을 편안하게 만나보세요.</p>
        {isLoggedIn ? (
          <div className="welcome-card"><span>로그인 완료</span><strong>오늘도 반가워요.</strong></div>
        ) : (
          <div className="hero-actions">
            <button className="primary-button" type="button" onClick={() => onNavigate("signup")}>회원가입하기</button>
            <button className="secondary-button" type="button" onClick={() => onNavigate("login")}>로그인</button>
          </div>
        )}
      </section>
      <aside className="hero-visual" aria-hidden="true">
        <div className="orb orb-large" />
        <div className="orb orb-small" />
        <div className="product-card">NEW<br /><span>COLLECTION</span></div>
      </aside>
    </main>
  );
}

function AuthLayout({ title, description, children, onNavigate }: { title: string; description: string; children: ReactNode; onNavigate: (page: Page) => void }) {
  return (
    <main className="auth-page">
      <button className="back-button" type="button" onClick={() => onNavigate("home")}>홈으로</button>
      <section className="auth-card">
        <p className="eyebrow">SHOP ACCOUNT</p>
        <h1>{title}</h1>
        <p className="auth-description">{description}</p>
        {children}
      </section>
    </main>
  );
}

function Signup({ onNavigate }: { onNavigate: (page: Page) => void }) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [name, setName] = useState("");
  const [message, setMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const signup = async (event: FormEvent) => {
    event.preventDefault();
    setMessage("");
    if (password.length < PASSWORD_MIN_LENGTH || password.length > PASSWORD_MAX_LENGTH) {
      setMessage("비밀번호는 8자 이상 20자 이하로 입력해 주세요.");
      return;
    }

    setIsSubmitting(true);
    try {
      await axios.post<SignupResponse>(`${API_URL}/signup`, { email, password, name }, { timeout: 5000 });
      onNavigate("login");
    } catch (error) {
      setMessage(getErrorMessage(error, "회원가입 중 오류가 발생했습니다."));
    } finally {
      setIsSubmitting(false);
    }
  };

  return <AuthLayout title="계정을 만들어 보세요" description="회원가입 후 바로 로그인할 수 있습니다." onNavigate={onNavigate}>
    <form className="auth-form" onSubmit={signup}>
      <Field label="이름"><input required value={name} onChange={(event) => setName(event.target.value)} /></Field>
      <Field label="이메일"><input required type="email" value={email} onChange={(event) => setEmail(event.target.value)} /></Field>
      <Field label="비밀번호"><input required type="password" minLength={PASSWORD_MIN_LENGTH} maxLength={PASSWORD_MAX_LENGTH} value={password} onChange={(event) => setPassword(event.target.value)} /></Field>
      <p className="field-help">8자 이상 20자 이하로 입력해 주세요.</p>
      {message && <p className="message error">{message}</p>}
      <button className="primary-button form-button" disabled={isSubmitting} type="submit">{isSubmitting ? "가입 중..." : "회원가입"}</button>
      <p className="form-switch">이미 계정이 있나요? <button type="button" onClick={() => onNavigate("login")}>로그인</button></p>
    </form>
  </AuthLayout>;
}

function Login({ onLogin, onNavigate }: { onLogin: (accessToken: string) => void; onNavigate: (page: Page) => void }) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const login = async (event: FormEvent) => {
    event.preventDefault();
    setMessage("");
    setIsSubmitting(true);
    try {
      const response = await axios.post<LoginResponse>(
        `${API_URL}/login`,
        { email, password },
        { timeout: 5000, withCredentials: true },
      );
      onLogin(response.data.accessToken);
    } catch (error) {
      setMessage(getErrorMessage(error, "로그인 중 오류가 발생했습니다."));
    } finally {
      setIsSubmitting(false);
    }
  };

  return <AuthLayout title="다시 만나서 반가워요" description="계정 정보로 로그인해 주세요." onNavigate={onNavigate}>
    <form className="auth-form" onSubmit={login}>
      <Field label="이메일"><input required type="email" value={email} onChange={(event) => setEmail(event.target.value)} /></Field>
      <Field label="비밀번호"><input required type="password" value={password} onChange={(event) => setPassword(event.target.value)} /></Field>
      {message && <p className="message error">{message}</p>}
      <button className="primary-button form-button" disabled={isSubmitting} type="submit">{isSubmitting ? "로그인 중..." : "로그인"}</button>
      <p className="form-switch">아직 계정이 없나요? <button type="button" onClick={() => onNavigate("signup")}>회원가입</button></p>
    </form>
  </AuthLayout>;
}

function Field({ label, children }: { label: string; children: ReactNode }) {
  return <label className="field"><span>{label}</span>{children}</label>;
}

function getErrorMessage(error: unknown, fallback: string) {
  if (!axios.isAxiosError(error) || !error.response) return "서버에 연결할 수 없습니다. 서버 실행 상태를 확인해 주세요.";
  if (error.response.status === 409) return "이미 가입된 이메일입니다.";
  if (error.response.status === 400) return "입력값을 다시 확인해 주세요.";
  if (error.response.status === 401) return "이메일 또는 비밀번호가 올바르지 않습니다.";
  return fallback;
}

export default App;
