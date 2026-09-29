import SwiftUI

extension View {
    /// Tags a screen/row container without overwriting the identifiers of its children
    /// (a bare `.accessibilityIdentifier` on a stack propagates to every child).
    func accessibilityContainer(_ id: String) -> some View {
        accessibilityElement(children: .contain).accessibilityIdentifier(id)
    }

    /// Adds a "Done" button above the keyboard so tests (and number-pad users) can dismiss it.
    func keyboardDoneButton() -> some View {
        toolbar {
            ToolbarItemGroup(placement: .keyboard) {
                Spacer()
                Button("Done") {
                    UIApplication.shared.sendAction(
                        #selector(UIResponder.resignFirstResponder), to: nil, from: nil, for: nil
                    )
                }
                .accessibilityIdentifier(AID.Keyboard.done)
            }
        }
    }
}
