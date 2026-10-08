'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import {
  ArrowLeft, Plus, Pencil, Trash2, CheckCircle, XCircle,
  Box, Users, History, Save, X,
} from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { useAuth } from '@/lib/auth-context';
import {
  resourcesApi, employeeAdminApi, administrationApi,
  Resource, ResourceCreate, EmployeeWithLimits, AllocationRequest,
} from '@/lib/api';
import { useToast } from '@/hooks/use-toast';

const TYPE_LABELS: Record<string, string> = {
  DESK: 'Biurko', CONFERENCE_ROOM: 'Sala konf.', LAPTOP: 'Laptop',
  MONITOR: 'Monitor', PROJECTOR: 'Projektor', VEHICLE: 'Pojazd', ACCESSORY: 'Akcesoria',
};
const RESOURCE_TYPES = Object.keys(TYPE_LABELS);

const STATUS_LABELS: Record<string, { label: string; color: string }> = {
  SUBMITTED: { label: 'Złożony', color: 'bg-yellow-100 text-yellow-800' },
  APPROVED: { label: 'Zatwierdzony', color: 'bg-blue-100 text-blue-800' },
  ACTIVE: { label: 'Aktywny', color: 'bg-green-100 text-green-800' },
  COMPLETED: { label: 'Zakończony', color: 'bg-gray-100 text-gray-700' },
  REJECTED: { label: 'Odrzucony', color: 'bg-red-100 text-red-800' },
  CANCELLED: { label: 'Anulowany', color: 'bg-gray-100 text-gray-500' },
  OVERDUE: { label: 'Przeterminowane', color: 'bg-orange-100 text-orange-800' },
};

const EMPTY_FORM: ResourceCreate = { type: 'DESK', name: '', building: '', floor: 1, number: '' };

export default function ItAdminPage() {
  const { user, loading: authLoading, isItAdmin } = useAuth();
  const router = useRouter();
  const { toast } = useToast();

  // --- Resources ---
  const [resources, setResources] = useState<Resource[]>([]);
  const [resLoading, setResLoading] = useState(true);
  const [resDialog, setResDialog] = useState<'add' | 'edit' | null>(null);
  const [editTarget, setEditTarget] = useState<Resource | null>(null);
  const [form, setForm] = useState<ResourceCreate>(EMPTY_FORM);

  // --- Limits ---
  const [employees, setEmployees] = useState<EmployeeWithLimits[]>([]);
  const [empLoading, setEmpLoading] = useState(true);
  const [editingLimit, setEditingLimit] = useState<{ empId: string; type: string; value: number } | null>(null);

  // --- History ---
  const [history, setHistory] = useState<AllocationRequest[]>([]);
  const [histLoading, setHistLoading] = useState(true);
  const [histFilter, setHistFilter] = useState('');
  const [resourceMap, setResourceMap] = useState<Record<string, Resource>>({});

  useEffect(() => {
    if (authLoading) return;

    if (!user) { router.push('/login'); return; }
    if (!isItAdmin()) { router.push('/'); return; }
    loadResources();
    loadEmployees();
    loadHistory();
  }, [user, authLoading, router]);

  const loadResources = () =>
    resourcesApi.list().then(setResources).catch(() => {}).finally(() => setResLoading(false));

  const loadEmployees = () =>
    employeeAdminApi.list().then(setEmployees).catch(() => {}).finally(() => setEmpLoading(false));

  const loadHistory = () =>
    Promise.all([administrationApi.requests(), resourcesApi.list()])
      .then(([reqs, res]) => {
        setHistory(reqs);
        setResourceMap(Object.fromEntries(res.map(r => [r.id, r])));
      })
      .catch(() => {})
      .finally(() => setHistLoading(false));

  // --- Resources actions ---
  const openAdd = () => { setForm(EMPTY_FORM); setEditTarget(null); setResDialog('add'); };
  const openEdit = (r: Resource) => {
    setForm({ type: r.type, name: r.name, building: r.building, floor: r.floor, number: r.number });
    setEditTarget(r);
    setResDialog('edit');
  };

  const handleSaveResource = async () => {
    try {
      if (resDialog === 'add') {
        await resourcesApi.add(form);
        toast({ title: 'Zasób dodany' });
      } else if (editTarget) {
        await resourcesApi.update(editTarget.id, form);
        toast({ title: 'Zasób zaktualizowany' });
      }
      setResDialog(null);
      loadResources();
    } catch (e: unknown) {
      toast({ title: 'Błąd', description: e instanceof Error ? e.message : 'Błąd', variant: 'destructive' });
    }
  };

  const handleToggleAvailability = async (r: Resource) => {
    try {
      if (r.available) await resourcesApi.markUnavailable(r.id);
      else await resourcesApi.markAvailable(r.id);
      loadResources();
    } catch (e: unknown) {
      toast({ title: 'Błąd', description: e instanceof Error ? e.message : 'Błąd', variant: 'destructive' });
    }
  };

  const handleDeactivate = async (id: string) => {
    try {
      await resourcesApi.deactivate(id);
      toast({ title: 'Zasób dezaktywowany' });
      loadResources();
    } catch (e: unknown) {
      toast({ title: 'Błąd', description: e instanceof Error ? e.message : 'Błąd', variant: 'destructive' });
    }
  };

  // --- Limits actions ---
  const handleSaveLimit = async () => {
    if (!editingLimit) return;
    try {
      await employeeAdminApi.upsertLimit(editingLimit.empId, editingLimit.type, editingLimit.value);
      toast({ title: 'Limit zaktualizowany' });
      setEditingLimit(null);
      loadEmployees();
    } catch (e: unknown) {
      toast({ title: 'Błąd', description: e instanceof Error ? e.message : 'Błąd', variant: 'destructive' });
    }
  };

  const filteredHistory = histFilter
    ? history.filter(r => r.status === histFilter)
    : history;

  return (
    <div className="min-h-screen bg-gray-50 p-8">
      <div className="max-w-6xl mx-auto">
        <div className="flex items-center gap-4 mb-6">
          <Link href="/"><Button variant="ghost" size="sm"><ArrowLeft className="h-4 w-4 mr-1" />Powrót</Button></Link>
          <div>
            <h1 className="text-2xl font-bold">Panel IT Admin</h1>
            <p className="text-gray-600 text-sm">Zarządzanie zasobami, limitami i historia zdarzeń</p>
          </div>
        </div>

        <Tabs defaultValue="resources">
          <TabsList className="mb-6">
            <TabsTrigger value="resources" className="gap-2"><Box className="h-4 w-4" />Zasoby</TabsTrigger>
            <TabsTrigger value="limits" className="gap-2"><Users className="h-4 w-4" />Limity pracowników</TabsTrigger>
            <TabsTrigger value="history" className="gap-2"><History className="h-4 w-4" />Historia zdarzeń</TabsTrigger>
          </TabsList>

          {/* ===== RESOURCES ===== */}
          <TabsContent value="resources">
            <div className="flex justify-between items-center mb-4">
              <p className="text-sm text-gray-500">{resources.length} zasobów w systemie</p>
              <Button size="sm" className="gap-2" onClick={openAdd}>
                <Plus className="h-4 w-4" />Dodaj zasób
              </Button>
            </div>
            {resLoading ? (
              <p className="text-gray-500">Ładowanie...</p>
            ) : (
              <div className="space-y-2">
                {resources.map(r => (
                  <Card key={r.id} className={`${!r.available ? 'opacity-60' : ''}`}>
                    <CardContent className="p-4 flex items-center justify-between gap-4">
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-2 flex-wrap">
                          <span className="font-medium text-sm">{r.name}</span>
                          <span className="text-xs bg-blue-50 text-blue-700 px-1.5 py-0.5 rounded">
                            {TYPE_LABELS[r.type] || r.type}
                          </span>
                          {r.available
                            ? <span className="text-xs text-green-600 flex items-center gap-1"><CheckCircle className="h-3 w-3" />Dostępny</span>
                            : <span className="text-xs text-red-500 flex items-center gap-1"><XCircle className="h-3 w-3" />Niedostępny</span>
                          }
                        </div>
                        <p className="text-xs text-gray-500 mt-0.5">{r.building}, piętro {r.floor}, nr {r.number}</p>
                      </div>
                      <div className="flex gap-2 flex-shrink-0">
                        <Button size="sm" variant="outline" onClick={() => handleToggleAvailability(r)}>
                          {r.available ? 'Zablokuj' : 'Odblokuj'}
                        </Button>
                        <Button size="sm" variant="outline" onClick={() => openEdit(r)}>
                          <Pencil className="h-3.5 w-3.5" />
                        </Button>
                        <Button size="sm" variant="outline" className="text-red-600 border-red-200 hover:bg-red-50"
                          onClick={() => handleDeactivate(r.id)}>
                          <Trash2 className="h-3.5 w-3.5" />
                        </Button>
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </div>
            )}
          </TabsContent>

          {/* ===== LIMITS ===== */}
          <TabsContent value="limits">
            {empLoading ? (
              <p className="text-gray-500">Ładowanie...</p>
            ) : (
              <div className="space-y-4">
                {employees.map(emp => (
                  <Card key={emp.id}>
                    <CardHeader className="pb-2">
                      <CardTitle className="text-base flex items-center gap-2">
                        {emp.firstName} {emp.lastName}
                        <span className="text-xs font-normal text-gray-500">{emp.email}</span>
                        <span className="text-xs bg-gray-100 text-gray-600 px-1.5 py-0.5 rounded">{emp.role}</span>
                      </CardTitle>
                    </CardHeader>
                    <CardContent className="pt-0">
                      <div className="flex flex-wrap gap-3">
                        {RESOURCE_TYPES.map(type => {
                          const current = emp.limits[type] ?? 0;
                          const isEditing = editingLimit?.empId === emp.id && editingLimit?.type === type;
                          return (
                            <div key={type} className="flex items-center gap-1.5 bg-gray-50 border rounded-lg px-3 py-2 text-sm">
                              <span className="text-gray-600 w-24 text-xs">{TYPE_LABELS[type]}</span>
                              {isEditing ? (
                                <>
                                  <Input
                                    type="number"
                                    min={0}
                                    className="w-16 h-7 text-center text-sm"
                                    value={editingLimit.value}
                                    onChange={e => setEditingLimit({ ...editingLimit, value: parseInt(e.target.value) || 0 })}
                                    autoFocus
                                  />
                                  <Button size="sm" variant="ghost" className="h-7 w-7 p-0 text-green-600" onClick={handleSaveLimit}>
                                    <Save className="h-3.5 w-3.5" />
                                  </Button>
                                  <Button size="sm" variant="ghost" className="h-7 w-7 p-0" onClick={() => setEditingLimit(null)}>
                                    <X className="h-3.5 w-3.5" />
                                  </Button>
                                </>
                              ) : (
                                <>
                                  <span className="font-semibold w-6 text-center">{current}</span>
                                  <Button
                                    size="sm" variant="ghost" className="h-7 w-7 p-0 text-gray-400 hover:text-gray-700"
                                    onClick={() => setEditingLimit({ empId: emp.id, type, value: current })}
                                  >
                                    <Pencil className="h-3 w-3" />
                                  </Button>
                                </>
                              )}
                            </div>
                          );
                        })}
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </div>
            )}
          </TabsContent>

          {/* ===== HISTORY ===== */}
          <TabsContent value="history">
            <div className="flex gap-2 mb-4 flex-wrap">
              {['', ...Object.keys(STATUS_LABELS)].map(s => (
                <Button key={s} variant={histFilter === s ? 'default' : 'outline'} size="sm"
                  onClick={() => setHistFilter(s)}>
                  {s === '' ? 'Wszystkie' : STATUS_LABELS[s]?.label}
                </Button>
              ))}
            </div>
            {histLoading ? (
              <p className="text-gray-500">Ładowanie...</p>
            ) : filteredHistory.length === 0 ? (
              <Card><CardContent className="p-8 text-center text-gray-500">Brak zdarzeń.</CardContent></Card>
            ) : (
              <div className="space-y-2">
                {filteredHistory.map(r => {
                  const res = resourceMap[r.resourceId];
                  return (
                    <Card key={r.id}>
                      <CardContent className="p-3 flex items-center gap-4">
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center gap-2 flex-wrap mb-1">
                            <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${STATUS_LABELS[r.status]?.color}`}>
                              {STATUS_LABELS[r.status]?.label}
                            </span>
                            <span className="text-xs text-gray-500">
                              {r.employeeFirstName && r.employeeLastName
                                ? `${r.employeeFirstName} ${r.employeeLastName}`
                                : r.employeeId.slice(0, 8)}
                            </span>
                            <span className="text-xs text-gray-400">→</span>
                            <span className="text-xs font-medium">{res ? res.name : r.resourceId.slice(0, 8)}</span>
                            {res && <span className="text-xs text-gray-400">{TYPE_LABELS[res.type] || res.type}</span>}
                          </div>
                          <div className="flex items-center gap-3 text-xs text-gray-500">
                            <span>{r.dateFrom} — {r.dateTo}</span>
                            <span>złożony: {new Date(r.submittedAt).toLocaleDateString('pl-PL')}</span>
                            {r.issuedAt && <span>wydany: {new Date(r.issuedAt).toLocaleDateString('pl-PL')}</span>}
                            {r.returnedAt && <span>zwrócony: {new Date(r.returnedAt).toLocaleDateString('pl-PL')}</span>}
                          </div>
                          {r.rejectionReason && <p className="text-xs text-red-500 mt-0.5">Powód: {r.rejectionReason}</p>}
                        </div>
                      </CardContent>
                    </Card>
                  );
                })}
              </div>
            )}
          </TabsContent>
        </Tabs>
      </div>

      {/* Resource add/edit dialog */}
      <Dialog open={!!resDialog} onOpenChange={v => !v && setResDialog(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{resDialog === 'add' ? 'Dodaj zasób' : 'Edytuj zasób'}</DialogTitle>
          </DialogHeader>
          <div className="space-y-3">
            {resDialog === 'add' && (
              <div>
                <Label>Typ</Label>
                <Select value={form.type} onValueChange={v => setForm(f => ({ ...f, type: v }))}>
                  <SelectTrigger><SelectValue /></SelectTrigger>
                  <SelectContent>
                    {RESOURCE_TYPES.map(t => <SelectItem key={t} value={t}>{TYPE_LABELS[t]}</SelectItem>)}
                  </SelectContent>
                </Select>
              </div>
            )}
            <div>
              <Label>Nazwa</Label>
              <Input value={form.name} onChange={e => setForm(f => ({ ...f, name: e.target.value }))} placeholder="np. Biurko A-105" />
            </div>
            <div className="grid grid-cols-3 gap-2">
              <div>
                <Label>Budynek</Label>
                <Input value={form.building} onChange={e => setForm(f => ({ ...f, building: e.target.value }))} placeholder="A" />
              </div>
              <div>
                <Label>Piętro</Label>
                <Input type="number" value={form.floor} onChange={e => setForm(f => ({ ...f, floor: parseInt(e.target.value) || 0 }))} />
              </div>
              <div>
                <Label>Numer</Label>
                <Input value={form.number} onChange={e => setForm(f => ({ ...f, number: e.target.value }))} placeholder="105" />
              </div>
            </div>
            <div className="flex gap-2 justify-end pt-2">
              <Button variant="outline" onClick={() => setResDialog(null)}>Anuluj</Button>
              <Button onClick={handleSaveResource} disabled={!form.name || !form.building || !form.number}>
                {resDialog === 'add' ? 'Dodaj' : 'Zapisz'}
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}
