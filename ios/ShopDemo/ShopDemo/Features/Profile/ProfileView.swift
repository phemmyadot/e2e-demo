import SwiftUI

struct ProfileView: View {
    let session: SessionStore
    let cart: CartStore

    var body: some View {
        NavigationStack {
            List {
                Section("Account") {
                    LabeledContent("Name", value: session.user?.name ?? "-")
                    LabeledContent("Email") {
                        Text(session.user?.email ?? "-")
                            .accessibilityIdentifier(AID.Profile.email)
                    }
                }
                Section {
                    Button("Sign Out", role: .destructive) {
                        cart.clear()
                        session.signOut()
                    }
                    .accessibilityIdentifier(AID.Profile.signOut)
                }
            }
            .navigationTitle("Profile")
            .accessibilityContainer(AID.Profile.screen)
        }
    }
}
