package jakebarnby.pop;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.view.WindowManager;

import com.jakebarnby.pop.R;

/**
 * A <code>Dialog</code> that fades in and out with the title section removed and is inflated with a custom layout
 * @author Jake Barnby
 *
 */

public class FadeDialog {

	private int customLayoutId;
	private Dialog dialog;

	public FadeDialog(Dialog dialog, int customLayoutId) {
		this.dialog = dialog;
		this.customLayoutId = customLayoutId;
	}

	/**
	 * Create and return the <code>FadeDialog</code>
	 */
	public Dialog getDialog() {
		// Removing the title of the dialog so custom one can be set
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		// Set custom layout to dialog
		dialog.setContentView(customLayoutId);
		// Don't allow the dialog to close because a user clicked outside of it
		dialog.setCanceledOnTouchOutside(false);
		//Set the dim level of the activity behind
		WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
		lp.dimAmount = 0.8f; // Dim level. 0.0 - no dim, 1.0 - completely opaque
		// Set fade in/out animation
		lp.windowAnimations = R.style.FadeDialogAnimation;
		dialog.getWindow().setAttributes(lp);
		
		return dialog;
	}
}
