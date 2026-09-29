<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\FeedStock;
use App\Models\FeedStockBatch;
use Illuminate\Http\Request;

class FeedStockController extends Controller
{
    public function index()
    {
        $feeds = FeedStock::withSum('batches as total_stock', 'quantity_remaining')->get();
        return response()->json($feeds);
    }

    public function store(Request $request)
    {
        $validated = $request->validate([
            'feed_name' => 'required|string|max:255', 
            'unit' => 'required|string|max:50',
        ]);

        $feed = FeedStock::create($validated);

        return response()->json($feed, 201);
    }

    public function update(Request $request, $id)
    {
        $validated = $request->validate([
            'feed_name' => 'required|string|max:255',
            'unit' => 'required|string|max:50',
        ]);

        $feed = FeedStock::findOrFail($id);
        $feed->update($validated);

        return response()->json($feed, 200);
    }

    public function destroy($id)
    {
        $feed = FeedStock::findOrFail($id);
        if ($feed->consumptions()->count() > 0) {
            return response()->json([
                'message' => 'Gagal menghapus: Pakan ini sudah memiliki riwayat pemakaian.'
            ], 422);
        }

        $feed->batches()->delete();
        $feed->delete();

        return response()->json(['message' => 'Pakan berhasil dihapus'], 200);
    }

    public function addBatch(Request $request)
    {
        $validated = $request->validate([
            'feed_id' => 'required|exists:feed_stocks,id',
            'quantity_in' => 'required|numeric|min:0.01',
            'entry_date' => 'required|date',
        ]);

        $batch = FeedStockBatch::create([
            'feed_id' => $validated['feed_id'],
            'quantity_in' => $validated['quantity_in'],
            'quantity_remaining' => $validated['quantity_in'],
            'entry_date' => $validated['entry_date'],
        ]);

        return response()->json($batch, 201);
    }
}