document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("registerForm");
    const msg = document.getElementById("registerMessage");

    form.addEventListener("submit", async (e) => {
      e.preventDefault();

      const username = document.getElementById("username").value.trim();
      const password = document.getElementById("password").value;
      const confirmPassword = document.getElementById("confirmPassword").value;

      if (password !== confirmPassword) {
        msg.style.color = "#fca5a5";
        msg.textContent = "⚠️ Passwords do not match!";
        return;
      }

      try {
        const resp = await fetch("/auth/register", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ username, password })
        });

        if (resp.ok) {
          msg.style.color = "#86efac";
          msg.textContent = "✅ Registration successful! Redirecting to login...";
          setTimeout(() => window.location.href = "/login-page", 2000);
        } else {
          const errorData = await resp.json();
          msg.style.color = "#fca5a5";
          msg.textContent = "⚠️ " + (errorData.error || "Registration failed.");
        }
      } catch (err) {
        msg.style.color = "#fca5a5";
        msg.textContent = "⚠️ Unable to register. Please try again later.";
      }
    });
  });