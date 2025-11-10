import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { DriverService, DriverDocument } from '../../../core/services/driver';

@Component({
  selector: 'app-driver-documents',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './driver-documents.html',
  styleUrls: ['./driver-documents.scss']
})
export class DriverDocumentsComponent implements OnInit {
  egn!: string;
  documents: DriverDocument[] = [];

  constructor(private route: ActivatedRoute, private driverService: DriverService) {}

  ngOnInit() {
    this.egn = this.route.snapshot.paramMap.get('egn')!;
    this.loadDocuments();
  }

  loadDocuments() {
    this.driverService.getDocuments(this.egn).subscribe(docs => this.documents = docs);
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.driverService.uploadDocument(this.egn, file).subscribe(() => this.loadDocuments());
    }
  }

  download(id: number, fileName: string) {
    this.driverService.downloadDocument(this.egn, id).subscribe(blob => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = fileName;
      a.click();
      URL.revokeObjectURL(url);
    });
  }

  delete(id: number) {
    if (confirm('Изтриване на документа?')) {
      this.driverService.deleteDocument(this.egn, id).subscribe(() => this.loadDocuments());
    }
  }
}
