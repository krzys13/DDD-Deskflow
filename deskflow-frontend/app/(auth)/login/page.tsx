'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Building2 } from 'lucide-react';
import { useAuth } from '@/lib/auth-context';
import { authApi } from '@/lib/api';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Label } from '@/components/ui/label';
import { useToast } from '@/hooks/use-toast';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const router = useRouter();
  const { toast } = useToast();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      const res = await authApi.login(email, password);
      login(res.token, {
        email: res.email,
        role: res.role,
        firstName: res.firstName,
        lastName: res.lastName,
        employeeId: res.employeeId,
      });
      router.push('/');
    } catch (err: unknown) {
      toast({
        title: 'Błąd logowania',
        description: err instanceof Error ? err.message : 'Nieprawidłowe dane logowania',
        variant: 'destructive',
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center space-y-2">
          <div className="flex justify-center">
            <Building2 className="h-10 w-10 text-blue-600" />
          </div>
          <CardTitle className="text-2xl font-bold">DeskFlow</CardTitle>
          <CardDescription>Zarządzanie zasobami biurowymi</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="email">Email</Label>
              <Input
                id="email"
                type="email"
                value={email}
                onChange={e => setEmail(e.target.value)}
                placeholder="jan.kowalski@firma.pl"
                required
                autoFocus
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="password">Hasło</Label>
              <Input
                id="password"
                type="password"
                value={password}
                onChange={e => setPassword(e.target.value)}
                required
              />
            </div>
            <Button type="submit" className="w-full" disabled={loading}>
              {loading ? 'Logowanie...' : 'Zaloguj się'}
            </Button>
          </form>
          <div className="mt-6 p-4 bg-slate-50 border rounded-lg text-sm text-slate-600">
            <p className="font-semibold mb-2">Dane testowe:</p>
            <div className="space-y-1">
              <p>👤 Pracownik: anna.kowalska@firma.pl / password123</p>
              <p>👔 Kierownik: marek.kierownik@firma.pl / password123</p>
              <p>⚙️ Admin IT: admin@firma.pl / admin123</p>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
