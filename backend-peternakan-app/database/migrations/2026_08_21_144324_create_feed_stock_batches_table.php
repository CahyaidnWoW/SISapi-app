<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
{
    Schema::create('feed_stock_batches', function (Blueprint $table) {
        $table->id();
        $table->foreignId('feed_id')->constrained('feed_stocks')->cascadeOnDelete();
        $table->decimal('quantity_in', 10, 2);
        $table->decimal('quantity_remaining', 10, 2);
        $table->date('entry_date');
        $table->timestamps();
    });
}

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('feed_stock_batches');
    }
};
