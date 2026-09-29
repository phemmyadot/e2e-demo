import SwiftUI

struct OrderConfirmationView: View {
    let order: Order
    let onDone: () -> Void

    var body: some View {
        VStack(spacing: 16) {
            Spacer()
            Image(systemName: "checkmark.seal.fill")
                .font(.system(size: 72))
                .foregroundStyle(.green)
                .accessibilityHidden(true)
            Text("Order confirmed")
                .font(.title.bold())
            Text(order.id)
                .font(.headline.monospaced())
                .accessibilityIdentifier(AID.Confirmation.orderNumber)
            Text("Total paid: \(order.total.currency)")
                .foregroundStyle(.secondary)
            Spacer()
            Button {
                onDone()
            } label: {
                Text("Continue Shopping").frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
            .controlSize(.large)
            .accessibilityIdentifier(AID.Confirmation.done)
        }
        .padding(24)
        .navigationBarBackButtonHidden()
        .accessibilityContainer(AID.Confirmation.screen)
    }
}
