package com.tsc.printutility;

import android.content.Intent;
import android.print.PrintAttributes;
import android.print.PrintAttributes.MediaSize;
import android.print.PrintJobInfo;
import android.print.PrinterCapabilitiesInfo;
import android.print.PrinterId;
import android.print.PrinterInfo;
import android.printservice.PrintDocument;
import android.printservice.PrintJob;
import android.printservice.PrintService;
import android.printservice.PrinterDiscoverySession;
import android.util.Log;
import android.widget.Toast;

import com.tsc.printutility.Util.FileUtil;
import com.tsc.printutility.View.MainActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MyPrintService extends PrintService {

	private static final String TAG = "MyPrintService";

	@Override
	protected PrinterDiscoverySession onCreatePrinterDiscoverySession() {
		Log.d(TAG, "onCreatePrinterDiscoverySession()");
		return new MyPrintDiscoverySession(this);
	}

	@Override
	protected void onRequestCancelPrintJob(PrintJob printJob) {
		Log.d(TAG, "onRequestCancelPrintJob()");
		printJob.cancel();
	}

	@Override
	protected void onPrintJobQueued(PrintJob printJob) {
		Log.d(TAG, "onPrintJobQueued()");
		PrintJobInfo printjobinfo = printJob.getInfo();
		PrintDocument printdocument = printJob.getDocument();
		if (printJob.isQueued()) {
			// return;
		}

		printJob.start();

		String path = FileUtil.exportFile(this, printdocument.getData());

		printJob.complete();

		// call pdf program
		File real_pdf_file = new File(path);
		Intent ii = new Intent(this, MainActivity.class);
		ii.putExtra(Constant.Extra.FILE_PATH, real_pdf_file.getAbsolutePath());
		ii.addFlags(FLAG_ACTIVITY_NEW_TASK);
		startActivity(ii);
		return;
	}

	public class MyPrintDiscoverySession extends PrinterDiscoverySession {
		private static final String TAG = "MyPrintDiscoverySession";
		private final MyPrintService myPrintService;

		public MyPrintDiscoverySession(MyPrintService myPrintService) {
			Log.d(TAG, "MyPrintDiscoverySession()");
			this.myPrintService = myPrintService;
		}

		@Override
		public void onStartPrinterDiscovery(List<PrinterId> priorityList) {
			Log.d(TAG, "onStartPrinterDiscovery()");
			List<PrinterInfo> printers = this.getPrinters();
			String name = "Tsc Utility";
			PrinterInfo myprinter = new PrinterInfo.Builder(myPrintService.generatePrinterId(name), name,
					PrinterInfo.STATUS_IDLE).build();
			printers.add(myprinter);
			addPrinters(printers);
		}

		@Override
		public void onStopPrinterDiscovery() {
			Log.d(TAG, "onStopPrinterDiscovery()");
		}

		/**
		 * 确定这些打印机存在
		 * 
		 * @param printerIds
		 */
		@Override
		public void onValidatePrinters(List<PrinterId> printerIds) {
			Log.d(TAG, "onValidatePrinters()");
		}

		/**
		 * 选择打印机时调用该方法更新打印机的状态，能力
		 * 
		 * @param printerId
		 */
		@Override
		public void onStartPrinterStateTracking(PrinterId printerId) {
			Log.d(TAG, "onStartPrinterStateTracking()");
			PrinterInfo printer = findPrinterInfo(printerId);

			if (printer != null) {
				PrinterCapabilitiesInfo capabilities = new PrinterCapabilitiesInfo.Builder(printerId)
						.setMinMargins(new PrintAttributes.Margins(0, 0, 0, 0))
						.addMediaSize(MediaSize.ISO_A4, true)
						.addResolution(new PrintAttributes.Resolution("R1", "200x200", 203, 203), true)
						.addResolution(new PrintAttributes.Resolution("R2", "300x300", 300, 300), false)
						.setColorModes(PrintAttributes.COLOR_MODE_COLOR | PrintAttributes.COLOR_MODE_MONOCHROME,
								PrintAttributes.COLOR_MODE_MONOCHROME)
						.build();

				printer = new PrinterInfo.Builder(printer).setCapabilities(capabilities)
						.setStatus(PrinterInfo.STATUS_IDLE)
						// .setDescription("fake print 1!")
						.build();
				List<PrinterInfo> printers = new ArrayList<PrinterInfo>();

				printers.add(printer);
				addPrinters(printers);
			}
		}

		@Override
		public void onStopPrinterStateTracking(PrinterId printerId) {
			Log.d(TAG, "onStopPrinterStateTracking()");
		}

		@Override
		public void onDestroy() {
			Log.d(TAG, "onDestroy()");
		}

		private PrinterInfo findPrinterInfo(PrinterId printerId) {
			List<PrinterInfo> printers = getPrinters();
			final int printerCount = getPrinters().size();
			for (int i = 0; i < printerCount; i++) {
				PrinterInfo printer = printers.get(i);
				if (printer.getId().equals(printerId)) {
					return printer;
				}
			}
			return null;
		}

		private void show_msg(String msg) {
			Toast toast = Toast.makeText(MyPrintService.this, msg, Toast.LENGTH_SHORT);
			toast.show();
		}

	}
}