import { useState, type FormEvent } from "react";
import "./App.css";
import axios from "axios";

type SignupResponse = {
  id: number;
  email: string;
  name: string;
};

const PASSWORD_MIN_LENGTH = 8;
const PASSWORD_MAX_LENGTH = 20;

function App() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [name, setName] = useState("");
  const [result, setResult] = useState<SignupResponse | null>(null);
  const [message, setMessage] = useState("");

  const passwordIsValid =
    password.length >= PASSWORD_MIN_LENGTH && password.length <= PASSWORD_MAX_LENGTH;

  const signup = async (event: FormEvent) => {
    event.preventDefault();
    setMessage("");
    setResult(null);

    if (!passwordIsValid) {
      setMessage("비밀번호는 8자 이상 20자 이하로 입력해 주세요.");
      return;
    }

    try {
      const response = await axios.post<SignupResponse>(
        "http://localhost:8080/api/members/signup",
        {email, password, name},
        {timeout: 5000},
      );

      setResult(response.data);
      setMessage("회원가입이 완료되었습니다.");
    } catch (error){
       if (axios.isAxiosError(error)) {
            if (!error.response) {
                setMessage("서버에 연결할 수 없습니다. Spring Boot 서버 실행 상태를 확인해 주세요.");
                return;
            }

            if (error.response.status === 409) {
                setMessage("이미 가입된 이메일입니다.");
                return;
            }

            if (error.response.status === 400) {
                setMessage("입력값을 다시 확인해 주세요.");
                return;
            }

            if (error.response.status >= 500) {
                setMessage("서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
                return;
            }
        }

        setMessage("회원가입 처리 중 알 수 없는 오류가 발생했습니다.");
    }
  };

  return (
    <main className="page">
      <form className="signup-form" onSubmit={signup}>
        <h1>회원가입</h1>

        <label>
          이름
          <input required value={name} onChange={(event) => setName(event.target.value)} />
        </label>

        <label>
          이메일
          <input
            required
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
          />
        </label>

        <label>
          비밀번호
          <input
            required
            type="password"
            minLength={PASSWORD_MIN_LENGTH}
            maxLength={PASSWORD_MAX_LENGTH}
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            aria-describedby="password-help"
          />
        </label>
        <p id="password-help" className={password && !passwordIsValid ? "field-error" : "field-help"}>
          비밀번호는 8자 이상 20자 이하로 입력해 주세요.
        </p>

        <button type="submit">가입하기</button>

        {message && <p className={result ? "message success" : "message error"}>{message}</p>}

        {result && (
          <section>
            <strong>가입된 회원</strong>
            <p>ID: {result.id}</p>
            <p>이메일: {result.email}</p>
            <p>이름: {result.name}</p>
          </section>
        )}
      </form>
    </main>
  );
}

export default App;
