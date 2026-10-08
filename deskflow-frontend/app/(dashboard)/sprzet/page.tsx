'use client';
import { useEffect } from 'react';
import { useRouter } from 'next/navigation';

export default function EquipmentRedirect() {
  const router = useRouter();
  useEffect(() => { router.replace('/biurka'); }, [router]);
  return null;
}
