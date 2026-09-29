import SwiftUI

struct LoginView: View {
    let session: SessionStore
    @State private var email = ""
    @State private var password = ""
    @State private var error: String?
    @State private var isLoading = false

    private var canSubmit: Bool { !email.isEmpty && !password.isEmpty && !isLoading }

    var body: some View {
        VStack(spacing: 20) {
            Spacer()
            Image(systemName: "bag.fill")
                .font(.system(size: 56))
                .foregroundStyle(.tint)
                .accessibilityHidden(true)
            Text("Welcome to ShopDemo")
                .font(.title.bold())
            Text("Sign in to continue")
                .foregroundStyle(.secondary)

            VStack(spacing: 12) {
                TextField("Email", text: $email)
                    .textContentType(.username)
                    .keyboardType(.emailAddress)
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()
                    .accessibilityLabel("Email")
                    .accessibilityIdentifier(AID.Login.email)
                SecureField("Password", text: $password)
                    .textContentType(.password)
                    .accessibilityLabel("Password")
                    .accessibilityIdentifier(AID.Login.password)
            }
            .textFieldStyle(.roundedBorder)

            if let error {
                Text(error)
                    .foregroundStyle(.red)
                    .font(.footnote)
                    .accessibilityIdentifier(AID.Login.error)
            }

            Button {
                Task { await submit() }
            } label: {
                ZStack {
                    Text("Sign In").opacity(isLoading ? 0 : 1)
                    if isLoading {
                        ProgressView().accessibilityIdentifier(AID.Login.spinner)
                    }
                }
                .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
            .controlSize(.large)
            .disabled(!canSubmit)
            .accessibilityLabel("Sign In")
            .accessibilityIdentifier(AID.Login.submit)

            Text("Hint: demo@shop.com / password123")
                .font(.caption)
                .foregroundStyle(.secondary)
            Spacer()
        }
        .padding(24)
        .keyboardDoneButton()
        .accessibilityContainer(AID.Login.screen)
    }

    private func submit() async {
        error = nil
        isLoading = true
        defer { isLoading = false }
        do {
            try await session.signIn(email: email, password: password)
        } catch {
            self.error = error.localizedDescription
        }
    }
}

#Preview {
    LoginView(session: SessionStore())
}
