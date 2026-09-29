<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Services\LivestockService;
use App\Models\Livestock;
use Illuminate\Http\Request;

class LivestockController extends Controller
{
    protected LivestockService $livestockService;

    public function __construct(LivestockService $livestockService)
    {
        $this->livestockService = $livestockService;
    }

    public function index()
    {
        return response()->json($this->livestockService->getAll());
    }

    public function store(Request $request)
    {
        $validated = $request->validate([
            'cage_id' => 'required|exists:cages,id',
            'breed_id' => 'required|exists:livestock_breeds,id',
            'gender' => 'required|in:male,female',
            'birth_date' => 'required|date',
            'entry_date' => 'required|date',
            'initial_weight' => 'required|numeric|min:0.01',
        ]);

        $livestock = $this->livestockService->create($validated);

        return response()->json($livestock, 201);
    }

    public function update(Request $request, $id)
{
    $livestock = Livestock::findOrFail($id);

    $validated = $request->validate([
        'tag_number' => 'required|string|unique:livestocks,tag_number,' . $id,
        'cage_id'    => 'required|exists:cages,id',
        'breed'      => 'required|string|max:100',
        'gender'     => 'required|in:jantan,betina',
        'birth_date' => 'nullable|date',
        'status'     => 'required|in:aktif,terjual,mati,afkir',
    ]);

    $livestock->update($validated);

    return response()->json([
        'message' => 'Data sapi berhasil diperbarui',
        'data'    => $livestock
    ]);
}

public function destroy($id)
{
    $livestock = Livestock::findOrFail($id);
    $hasHealthRecords = method_exists($livestock, 'healthRecords') && $livestock->healthRecords()->exists();
    $hasWeightLogs    = method_exists($livestock, 'weightLogs') && $livestock->weightLogs()->exists();

    if ($hasHealthRecords || $hasWeightLogs) {
        return response()->json([
            'message' => 'Sapi tidak dapat dihapus karena sudah memiliki riwayat medis/pakan. Silakan ubah status sapi menjadi Terjual atau Mati.'
        ], 400);
    }

    $livestock->delete();

    return response()->json([
        'message' => 'Data sapi berhasil dihapus'
    ]);
}

    public function showByTag(string $tagNumber)
    {
        $result = $this->livestockService->findByTag($tagNumber);

        if (!$result) {
            return response()->json(['message' => 'Sapi tidak ditemukan'], 404);
        }

        return response()->json($result);
    }
}