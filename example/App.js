import React, { useEffect, useState } from 'react';
import { SafeAreaView, Text } from 'react-native';
import BexPrintModule from 'bex-print-library';

export default function App() {
  const [message, setMessage] = useState('');

  useEffect(() => {
    BexPrintModule.getHelloMessage().then(setMessage);
  }, []);

  return (
    <SafeAreaView style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
      <Text>{message}</Text>
    </SafeAreaView>
  );
}
