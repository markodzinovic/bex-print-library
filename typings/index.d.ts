// typings/index.d.ts

export interface BluetoothDevice {
  name: string | null;
  address: string | null;
}

export interface BexPrintModuleType {
  getHelloMessage(): Promise<string>;

  connect(deviceName: string): Promise<string>;

  disconnect(): Promise<string>;

  print(data: string, lineWidth?: number): Promise<string>;

  listPairedDevices(): Promise<string[]>;

  listPairedDevicesWithAddress(): Promise<BluetoothDevice[]>;
}

declare const BexPrintModule: BexPrintModuleType;

export default BexPrintModule;
